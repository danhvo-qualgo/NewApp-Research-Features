package com.safeNest.demo.features.urlGuard.impl.urlGuard.view.floatingbutton;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.MainThread;
import androidx.core.content.ContextCompat;

import com.safeNest.demo.features.urlGuard.impl.R;
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus;
import com.safeNest.demo.features.urlGuard.impl.urlGuard.FloatingButtonFeature;

import java.lang.ref.WeakReference;

/**
 * A draggable floating button displayed as a system overlay over all apps.
 *
 * The button visual (oval background + centred shield icon) is set up
 * internally — no separate layout file or helper class is required.
 * Use {@link #update}, {@link #updateStatus}, or {@link #updateFeature}
 * to change the icon / colour at runtime.
 *
 * Features:
 *  - Drag anywhere on screen
 *  - Snap to nearest edge when finger is lifted
 *  - Auto-fade to semi-transparent after 1.5 s of inactivity
 *  - Long-press detection
 *  - Scale animation on press / release
 *
 * How to use:
 *   1. Create:  FloatingView view = new FloatingView(context);
 *   2. Call:    view.show()   → adds to WindowManager (visible on screen)
 *   3. Call:    view.hide()   → removes from WindowManager
 *   4. Update:  view.updateStatus(DetectionStatus.SAFE);
 */
public class FloatingView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {

    // ── Timing constants ──────────────────────────────────────────────────────

    private static final int   LONG_PRESS_TIMEOUT_MS = (int) (ViewConfiguration.getLongPressTimeout() * 1.5f);
    private static final long  IDLE_FADE_DELAY_MS    = 1500L;
    private static final long  SNAP_DURATION_MS      = 100L;
    private static final float DRAG_THRESHOLD_DP     = 8f;

    // ── Window management ─────────────────────────────────────────────────────

    private final WindowManager             windowManager;
    private final WindowManager.LayoutParams windowParams;
    private final DisplayMetrics            displayMetrics;

    // ── Touch / drag state ────────────────────────────────────────────────────

    private float   rawTouchX, rawTouchY;
    private float   touchDownX, touchDownY;
    private float   touchOffsetX, touchOffsetY;
    private int     windowXOnDown, windowYOnDown;
    private long    touchDownTime;

    private boolean isDragging        = false;
    private boolean draggingEnabled   = true;
    private boolean suppressNextClick = false;
    private boolean isReady           = false;

    // ── Position bounds ───────────────────────────────────────────────────────

    private final Rect positionLimitRect = new Rect();
    private int savedX = Integer.MIN_VALUE;
    private int savedY = Integer.MIN_VALUE;

    // ── Snap behaviour ────────────────────────────────────────────────────────

    private int moveDirection = 0;
    private int snapEdge      = 2;

    // ── Appearance ────────────────────────────────────────────────────────────

    private float idleAlpha = 0.5f;

    // ── System bar insets ─────────────────────────────────────────────────────

    private final int statusBarHeight;

    // ── Optional popup ────────────────────────────────────────────────────────

    private Dialog popup;

    // ── Listeners ─────────────────────────────────────────────────────────────

    public interface OnLongPressListener {
        void onLongPress(FloatingView view);
    }

    private OnLongPressListener  longPressListener;
    private View.OnTouchListener externalTouchListener;

    // ── Internal handlers / animators ─────────────────────────────────────────

    private final LongPressHandler   longPressHandler   = new LongPressHandler(this);
    private final Handler            idleHandler        = new Handler();
    private final Runnable           idleRunnable       = () ->
            animate().alpha(idleAlpha).setDuration(500).start();
    private       ValueAnimator      snapAnimator;
    private final LinearInterpolator linearInterpolator = new LinearInterpolator();

    // ── Button content ────────────────────────────────────────────────────────

    /** Oval background — colour is mutated on every status update. */
    private final GradientDrawable bgDrawable;
    /** Icon centred inside the button. */
    private final ImageView iconView;

    // ── Loading state ─────────────────────────────────────────────────────────

    /** Indeterminate spinner shown while an async operation is in progress. */
    private ProgressBar loadingView;
    /** True while loading is active — guards against re-entrant calls. */
    private boolean isLoading = false;
    /** Snapshots taken at the moment [showLoading] is called — restored by [hideLoading]. */
    private DetectionStatus    savedStatus;
    private FloatingButtonFeature savedFeature;
    /** Tracks the last applied values so we can restore them. */
    private DetectionStatus    currentStatus  = DetectionStatus.UNKNOWN;
    private FloatingButtonFeature currentFeature = FloatingButtonFeature.DEFAULT;

    // ─────────────────────────────────────────────────────────────────────────

    private static class LongPressHandler extends Handler {
        private final WeakReference<FloatingView> ref;
        LongPressHandler(FloatingView view) { ref = new WeakReference<>(view); }

        @Override
        public void handleMessage(Message msg) {
            FloatingView view = ref.get();
            if (view == null) { removeMessages(0); return; }
            view.isDragging = true;
            if (view.longPressListener != null) view.longPressListener.onLongPress(view);
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public FloatingView(Context context) {
        super(context);

        windowManager  = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        // ── WindowManager.LayoutParams ────────────────────────────────────────
        int buttonSize = (int) getResources().getDimension(R.dimen.floating_button_size);

        windowParams        = new WindowManager.LayoutParams();
        windowParams.width  = buttonSize;
        windowParams.height = buttonSize;
        windowParams.type   = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        windowParams.flags  = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        windowParams.format  = PixelFormat.TRANSLUCENT;
        windowParams.gravity = Gravity.TOP | Gravity.START;

        statusBarHeight = getSystemDimenPx(context.getResources(), "status_bar_height");

        // ── Button visual content ─────────────────────────────────────────────
        //   Oval background ── colour driven by DetectionStatus
        bgDrawable = new GradientDrawable();
        bgDrawable.setShape(GradientDrawable.OVAL);
        setBackground(bgDrawable);

        //   Centred icon ── drawable driven by FloatingButtonFeature
        int pad = (int) getResources().getDimension(R.dimen.floating_button_padding);
        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iconView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        iconView.setPadding(pad, pad, pad, pad);
        iconView.setLayoutParams(lp);
        addView(iconView);

        //   Apply defaults
        applyStatus(DetectionStatus.UNKNOWN);
        applyFeature(FloatingButtonFeature.DEFAULT);

        scheduleIdleFade();
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    // ── Status / feature update API ───────────────────────────────────────────

    /**
     * Swap icon and colour in one call (avoids two redraws).
     * Must be called on the **main thread**.
     */
    @MainThread
    public void update(FloatingButtonFeature feature, DetectionStatus status) {
        applyFeature(feature);
        applyStatus(status);
    }

    /**
     * Change only the background colour (e.g. after a URL scan result arrives).
     * Must be called on the **main thread**.
     */
    @MainThread
    public void updateStatus(DetectionStatus status) {
        applyStatus(status);
    }

    /**
     * Swap only the icon (e.g. when the active feature changes).
     * Must be called on the **main thread**.
     */
    @MainThread
    public void updateFeature(FloatingButtonFeature feature) {
        applyFeature(feature);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public WindowManager.LayoutParams getWindowLayoutParams() { return windowParams; }

    public void setIdleAlpha(float alpha) { idleAlpha = alpha; setAlpha(alpha); }

    public void setMoveDirection(int direction) {
        moveDirection = (savedX != Integer.MIN_VALUE) ? 5 : direction;
    }

    public void setSavedPosition(int x, int y) { savedX = x; savedY = y; }

    public void setDraggable(boolean on) { draggingEnabled = on; }

    public void setOnLongPressListener(OnLongPressListener listener) {
        this.longPressListener = listener;
    }

    public void setPopup(Dialog dialog) { this.popup = dialog; }

    public void setSafeInsetRect(Rect r) { /* reserved for notch / cutout handling */ }

    @Override
    public void setOnTouchListener(View.OnTouchListener listener) {
        externalTouchListener = listener;
    }

    // ── Show / hide ───────────────────────────────────────────────────────────

    public void show() {
        setScale(1.0f);
        try { windowManager.addView(this, windowParams); }
        catch (Exception e) { e.printStackTrace(); }
        scheduleIdleFade();
    }

    public void hide() {
        setScale(1.0f);
        try { windowManager.removeView(this); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void showInWindow() {
        setVisibility(VISIBLE);
        updatePositionBounds();
        setAlpha(0.9f);
        scheduleIdleFade();
    }

    public void hideInWindow() { setVisibility(GONE); }

    public void resize(int sizePercent, float newAlpha) {
        idleAlpha = newAlpha;
        setAlpha(newAlpha);
        int base = (int) getResources().getDimension(R.dimen.floating_button_size);
        windowParams.width = windowParams.height = (int) (base * sizePercent / 100f);
        try { windowManager.updateViewLayout(this, windowParams); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ── Loading overlay ───────────────────────────────────────────────────────

    /**
     * Swap the icon for an indeterminate circular spinner, signalling that an
     * async operation (e.g. URL analysis) is in progress.
     *
     * The current [DetectionStatus] and [FloatingButtonFeature] are saved and
     * fully restored when [hideLoading] is called.
     * Safe to call from any thread — posts to the main thread internally.
     */
    @MainThread
    public void showLoading() {
        if (isLoading) return;
        isLoading = true;

        // Snapshot state so we can restore it later.
        savedStatus  = currentStatus;
        savedFeature = currentFeature;

        // Hide the feature icon.
        iconView.setVisibility(INVISIBLE);

        // Lazy-create the spinner the first time it is needed.
        if (loadingView == null) {
            loadingView = new ProgressBar(getContext());
            loadingView.setIndeterminate(true);
            loadingView.setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));
            int spinnerSize = (int) (getResources().getDimension(R.dimen.floating_button_size) * 0.55f);
            LayoutParams lp = new LayoutParams(spinnerSize, spinnerSize, Gravity.CENTER);
            addView(loadingView, lp);
        }
        loadingView.setVisibility(VISIBLE);

        // Stay fully opaque and non-draggable while loading.
        cancelIdleFade();
        setAlpha(1.0f);
        draggingEnabled = false;
    }

    /**
     * Remove the loading spinner and restore the button to the icon + colours
     * it had before [showLoading] was called.
     * Safe to call even if loading is not currently active (no-op).
     */
    @MainThread
    public void hideLoading() {
        if (!isLoading) return;
        isLoading = false;

        // Restore appearance.
        if (loadingView != null) loadingView.setVisibility(GONE);
        iconView.setVisibility(VISIBLE);
        if (savedStatus  != null) applyStatus(savedStatus);
        if (savedFeature != null) applyFeature(savedFeature);

        // Re-enable dragging and schedule the normal idle fade.
        draggingEnabled = true;
        scheduleIdleFade();
    }

    // ── Touch / drag ──────────────────────────────────────────────────────────

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getVisibility() != VISIBLE || !draggingEnabled) return true;

        rawTouchX = event.getRawX();
        rawTouchY = event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                cancelSnap();
                cancelIdleFade();

                touchDownX    = rawTouchX;
                touchDownY    = rawTouchY;
                touchOffsetX  = event.getX();
                touchOffsetY  = event.getY();
                windowXOnDown = windowParams.x;
                windowYOnDown = windowParams.y;
                isDragging    = false;

                setScale(0.92f);
                moveWindowTo(calcWindowX(), calcWindowY());

                longPressHandler.removeMessages(0);
                longPressHandler.sendEmptyMessageDelayed(0, LONG_PRESS_TIMEOUT_MS);
                touchDownTime = event.getDownTime();
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    suppressNextClick = false;
                    longPressHandler.removeMessages(0);
                }
                if (touchDownTime != event.getDownTime()) break;

                boolean dialogOpen = popup != null && popup.isShowing();
                float   threshold  = displayMetrics.density * DRAG_THRESHOLD_DP;

                if (!dialogOpen && (isDragging
                        || Math.abs(rawTouchX - touchDownX) >= threshold
                        || Math.abs(rawTouchY - touchDownY) >= threshold)) {
                    isDragging = true;
                    moveWindowTo(calcWindowX(), calcWindowY());
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean wasSuppressed = suppressNextClick;
                suppressNextClick = false;
                longPressHandler.removeMessages(0);

                if (touchDownTime != event.getDownTime()) break;

                setScale(1.0f);

                if (isDragging) {
                    snapToEdge(true);
                } else if (!wasSuppressed) {
                    performClick();
                    for (int i = 0; i < getChildCount(); i++) {
                        getChildAt(i).performClick();
                    }
                }
                scheduleIdleFade();
                break;
        }

        if (externalTouchListener != null) externalTouchListener.onTouch(this, event);
        return true;
    }

    // ── View lifecycle ────────────────────────────────────────────────────────

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);

        if (moveDirection == 5 && savedX != Integer.MIN_VALUE) {
            windowParams.x = savedX;
            windowParams.y = savedY;
        } else {
            updatePositionBounds();
            windowParams.x = positionLimitRect.right;
            windowParams.y = (displayMetrics.heightPixels / 2) - (getMeasuredHeight() / 2);
        }

        isReady = true;
        try { windowManager.updateViewLayout(this, windowParams); }
        catch (IllegalArgumentException ignored) {}
        return true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        updatePositionBounds();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePositionBounds();
    }

    @Override
    public void onDetachedFromWindow() {
        if (snapAnimator != null) snapAnimator.removeAllUpdateListeners();
        super.onDetachedFromWindow();
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility != VISIBLE) {
            cancelLongPress();
            setScale(1.0f);
            if (isDragging) snapToEdge(false);
            longPressHandler.removeMessages(0);
        }
        super.setVisibility(visibility);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void applyStatus(DetectionStatus status) {
        currentStatus = status;
        int fillColor = ContextCompat.getColor(getContext(), status.getColorRes());
        bgDrawable.setColor(fillColor);

        // Tinted semi-transparent stroke: blend fill colour with white at 60 %
        int r = (int) (Color.red(fillColor)   * 0.4f + 255 * 0.6f);
        int g = (int) (Color.green(fillColor) * 0.4f + 255 * 0.6f);
        int b = (int) (Color.blue(fillColor)  * 0.4f + 255 * 0.6f);
        int strokeColor = Color.argb(128, r, g, b);
        int strokePx    = (int) (getResources().getDisplayMetrics().density * 1.5f + 0.5f);
        bgDrawable.setStroke(strokePx, strokeColor);
    }

    private void applyFeature(FloatingButtonFeature feature) {
        currentFeature = feature;
        iconView.setImageResource(feature.getIconRes());
    }

    // ── Position calculation ──────────────────────────────────────────────────

    private int calcWindowX() { return (int) (rawTouchX - touchOffsetX); }

    private int calcWindowY() { return (int) (windowYOnDown + (rawTouchY - touchDownY)); }

    private void moveWindowTo(int x, int y) {
        windowParams.x = x;
        windowParams.y = y;
        try { windowManager.updateViewLayout(this, windowParams); }
        catch (Exception ignored) {}
    }

    // ── Edge-snap ─────────────────────────────────────────────────────────────

    private void snapToEdge(boolean animated) {
        int curX = calcWindowX();
        int curY = calcWindowY();
        int targetX, targetY;

        if (moveDirection == 0) {
            int bottomEdge   = positionLimitRect.bottom;
            int buttonHeight = windowParams.height;

            if (curY > bottomEdge - buttonHeight) {
                snapEdge = 3; targetX = curX; targetY = bottomEdge;
            } else if (curY < buttonHeight) {
                snapEdge = 4; targetX = curX; targetY = positionLimitRect.top;
            } else {
                snapEdge = 2;
                int midScreen = (displayMetrics.widthPixels - getWidth()) / 2;
                targetX = curX > midScreen ? positionLimitRect.right : positionLimitRect.left;
                targetY = curY;
            }

        } else if (moveDirection == 2) {
            snapEdge = 2;
            int midScreen = (displayMetrics.widthPixels - getWidth()) / 2;
            targetX = curX > midScreen ? positionLimitRect.right : positionLimitRect.left;
            targetY = curY;

        } else {
            targetX = curX;
            targetY = curY;
        }

        targetX = Math.min(Math.max(positionLimitRect.left, targetX), positionLimitRect.right);
        targetY = Math.min(Math.max(positionLimitRect.top,  targetY), positionLimitRect.bottom);

        if (animated) animateSnap(curX, curY, targetX, targetY);
        else if (windowParams.x != targetX || windowParams.y != targetY) moveWindowTo(targetX, targetY);

        resetDragState();
    }

    private void animateSnap(int fromX, int fromY, int toX, int toY) {
        ValueAnimator anim;
        ValueAnimator.AnimatorUpdateListener listener;

        if (snapEdge == 3 || snapEdge == 4) {
            windowParams.x = toX;
            anim     = ValueAnimator.ofInt(fromY, toY);
            listener = va -> { windowParams.y = (int) va.getAnimatedValue();
                try { windowManager.updateViewLayout(this, windowParams); } catch (IllegalArgumentException ignored) {} };
        } else {
            windowParams.y = toY;
            anim     = ValueAnimator.ofInt(fromX, toX);
            listener = va -> { windowParams.x = (int) va.getAnimatedValue();
                try { windowManager.updateViewLayout(this, windowParams); } catch (IllegalArgumentException ignored) {} };
        }

        anim.addUpdateListener(listener);
        anim.setDuration(SNAP_DURATION_MS);
        anim.setInterpolator(linearInterpolator);
        anim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator a)  {}
            @Override public void onAnimationRepeat(Animator a) {}
            @Override public void onAnimationCancel(Animator a) {}
            @Override public void onAnimationEnd(Animator a)    { scheduleIdleFade(); }
        });
        anim.start();
        snapAnimator = anim;
    }

    private void resetDragState() {
        touchOffsetX = touchOffsetY = 0;
        touchDownX   = touchDownY   = 0;
        windowXOnDown = windowYOnDown = 0;
        isDragging = false;
    }

    // ── Position bounds update ────────────────────────────────────────────────

    private void updatePositionBounds() {
        cancelSnap();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenW = displayMetrics.widthPixels;
        int screenH = displayMetrics.heightPixels;
        positionLimitRect.set(0, 0, screenW - getMeasuredWidth(), screenH - statusBarHeight - getMeasuredHeight());
    }

    // ── Idle-fade helpers ─────────────────────────────────────────────────────

    private void scheduleIdleFade() {
        idleHandler.removeCallbacks(idleRunnable);
        idleHandler.postDelayed(idleRunnable, IDLE_FADE_DELAY_MS);
    }

    private void cancelIdleFade() {
        idleHandler.removeCallbacks(idleRunnable);
        animate().cancel();
        setAlpha(0.9f);
    }

    // ── Snap animation helpers ────────────────────────────────────────────────

    private void cancelSnap() {
        if (snapAnimator != null && snapAnimator.isStarted()) {
            snapAnimator.cancel();
            snapAnimator = null;
        }
    }

    // ── Misc helpers ──────────────────────────────────────────────────────────

    private void setScale(float scale) { setScaleX(scale); setScaleY(scale); }

    private static int getSystemDimenPx(Resources res, String name) {
        int id = res.getIdentifier(name, "dimen", "android");
        return id > 0 ? res.getDimensionPixelSize(id) : 0;
    }
}
