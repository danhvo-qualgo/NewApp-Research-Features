package com.safeNest.demo.features.urlGuard.impl.urlGuard.util

/**
 * Calculates the optimal (x, y) position for the floating action card so that:
 *  - The card is always near the floating button.
 *  - The card never overlaps the button.
 *  - The card never goes off screen.
 *
 * ── Algorithm: two-phase "Push & Shift" ──────────────────────────────────────
 *
 *  Phase 1 – Horizontal placement (LEFT / RIGHT of button)
 *   a) Preferred side = opposite of whichever screen-half the button centre is in.
 *      (button on the left half → card goes to the RIGHT, and vice-versa)
 *   b) If the card overflows the screen on the preferred side → flip to opposite.
 *   c) Once the horizontal position is fixed, align the card's vertical centre
 *      with the button's vertical centre.
 *      - If that causes vertical overflow or overlap with the button →
 *        push the card DOWN below the button, then try UP above it.
 *
 *  Phase 2 – Vertical placement (ABOVE / BELOW button)
 *   Only reached when Phase 1 cannot find space on either side.
 *   Same logic applied on the vertical axis; horizontal centre is clamped.
 *
 *  Phase 3 – Clamp fallback
 *   If neither phase finds a fitting position the card is placed at the Phase-1
 *   preferred position and clamped to the screen bounds.
 *
 * ── Coordinate system ────────────────────────────────────────────────────────
 *   All values are in raw pixels, using the same origin as
 *   WindowManager.LayoutParams with Gravity.TOP | Gravity.START.
 *   (0,0) = top-left corner of the screen.
 */
object CardPositionCalculator {

    private const val MARGIN_DP  = 8f   // gap between button and card (dp)
    private const val PADDING_DP = 12f  // minimum gap between card and screen edge (dp)

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Resolves the best position for the action card.
     *
     * @param btnX         Left edge of the floating button (px)
     * @param btnY         Top  edge of the floating button (px)
     * @param btnWidth     Width  of the floating button (px)
     * @param btnHeight    Height of the floating button (px)
     * @param cardWidth    Measured width  of the action card (px)
     * @param cardHeight   Measured height of the action card (px)
     * @param screenWidth  Full screen width  (px)
     * @param screenHeight Full screen height (px)
     * @param density      Display density scalar (dp → px)
     * @return             [CardPlacement] with the resolved top-left (x, y) for the card
     */
    fun resolve(
        btnX: Int, btnY: Int,
        btnWidth: Int, btnHeight: Int,
        cardWidth: Int, cardHeight: Int,
        screenWidth: Int, screenHeight: Int,
        density: Float
    ): CardPlacement {
        val margin  = (MARGIN_DP  * density + 0.5f).toInt()
        val padding = (PADDING_DP * density + 0.5f).toInt()

        val btn  = BtnBounds(btnX, btnY, btnWidth, btnHeight)
        val card = CardSize(cardWidth, cardHeight)

        // ── Phase 1: horizontal ───────────────────────────────────────────────
        val preferH = if (btn.centerX <= screenWidth / 2) HSide.RIGHT else HSide.LEFT

        tryHorizontal(preferH,          btn, card, screenWidth, screenHeight, margin, padding)
            ?.let { return it }
        tryHorizontal(preferH.flipped(), btn, card, screenWidth, screenHeight, margin, padding)
            ?.let { return it }

        // ── Phase 2: vertical ─────────────────────────────────────────────────
        val preferV = if (btn.centerY <= screenHeight / 2) VSide.BOTTOM else VSide.TOP

        tryVertical(preferV,          btn, card, screenWidth, screenHeight, margin, padding)
            ?.let { return it }
        tryVertical(preferV.flipped(), btn, card, screenWidth, screenHeight, margin, padding)
            ?.let { return it }

        // ── Phase 3: clamp fallback ───────────────────────────────────────────
        val (rawX, rawY) = horizontalRawXY(preferH, btn, card, margin)
        return CardPlacement(
            x     = rawX.coerceIn(padding, screenWidth  - card.w - padding),
            y     = rawY.coerceIn(padding, screenHeight - card.h - padding),
            hSide = preferH,
            vSide = preferV
        )
    }

    // ── Phase 1: try placing card LEFT or RIGHT of button ─────────────────────

    private fun tryHorizontal(
        hSide: HSide,
        btn: BtnBounds, card: CardSize,
        screenW: Int, screenH: Int,
        margin: Int, padding: Int
    ): CardPlacement? {
        val cardX = when (hSide) {
            HSide.RIGHT -> btn.right  + margin
            HSide.LEFT  -> btn.left   - card.w - margin
        }

        // Card must fit horizontally inside the screen
        if (cardX < padding || cardX + card.w > screenW - padding) return null

        // Align card vertically with button centre, then push if needed
        val rawY  = btn.centerY - card.h / 2
        val cardY = pushVertically(rawY, card, btn, screenH, margin, padding) ?: return null

        val vSide = if (cardY >= btn.bottom) VSide.BOTTOM else VSide.TOP
        return CardPlacement(cardX, cardY, hSide, vSide)
    }

    // ── Phase 2: try placing card ABOVE or BELOW button ───────────────────────

    private fun tryVertical(
        vSide: VSide,
        btn: BtnBounds, card: CardSize,
        screenW: Int, screenH: Int,
        margin: Int, padding: Int
    ): CardPlacement? {
        val cardY = when (vSide) {
            VSide.BOTTOM -> btn.bottom + margin
            VSide.TOP    -> btn.top    - card.h - margin
        }

        // Card must fit vertically inside the screen
        if (cardY < padding || cardY + card.h > screenH - padding) return null

        // Align card horizontally with button centre, then clamp to screen
        val rawX  = btn.centerX - card.w / 2
        val cardX = rawX.coerceIn(padding, screenW - card.w - padding)

        val hSide = if (cardX + card.w / 2 >= btn.centerX) HSide.RIGHT else HSide.LEFT
        return CardPlacement(cardX, cardY, hSide, vSide)
    }

    // ── Vertical push helper ──────────────────────────────────────────────────

    /**
     * Given a raw Y that centres the card on the button, return a corrected Y that:
     *  1. Keeps the card within [padding … screenH - cardH - padding]
     *  2. Does not overlap the button
     *
     * Returns null if no fitting vertical position exists.
     */
    private fun pushVertically(
        rawY: Int,
        card: CardSize, btn: BtnBounds,
        screenH: Int, margin: Int, padding: Int
    ): Int? {
        // Clamp to screen first
        val clampedY = rawY.coerceIn(padding, screenH - card.h - padding)

        // Check if clamped position still overlaps the button
        val overlapsBtn = clampedY < btn.bottom && clampedY + card.h > btn.top
        if (!overlapsBtn) return clampedY

        // Push DOWN — place card below the button
        val pushDown = btn.bottom + margin
        if (pushDown + card.h <= screenH - padding) return pushDown

        // Push UP — place card above the button
        val pushUp = btn.top - card.h - margin
        if (pushUp >= padding) return pushUp

        return null // completely out of space on this horizontal side
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private fun horizontalRawXY(
        hSide: HSide, btn: BtnBounds, card: CardSize, margin: Int
    ): Pair<Int, Int> {
        val x = when (hSide) {
            HSide.RIGHT -> btn.right + margin
            HSide.LEFT  -> btn.left  - card.w - margin
        }
        return x to (btn.centerY - card.h / 2)
    }

    // ── Internal geometry models ──────────────────────────────────────────────

    private data class BtnBounds(val left: Int, val top: Int, val w: Int, val h: Int) {
        val right   get() = left + w
        val bottom  get() = top  + h
        val centerX get() = left + w / 2
        val centerY get() = top  + h / 2
    }

    private data class CardSize(val w: Int, val h: Int)

    // ── Side enums ────────────────────────────────────────────────────────────

    enum class HSide {
        LEFT, RIGHT;
        fun flipped() = if (this == RIGHT) LEFT else RIGHT
    }

    enum class VSide {
        TOP, BOTTOM;
        fun flipped() = if (this == BOTTOM) TOP else BOTTOM
    }
}

// ── Public result type ────────────────────────────────────────────────────────

/**
 * The resolved top-left position for the action card in screen-pixel coordinates
 * (origin = top-left of screen, same as WindowManager Gravity.TOP|Gravity.START).
 */
data class CardPlacement(
    /** Absolute X (px) for the card's left edge. */
    val x: Int,
    /** Absolute Y (px) for the card's top edge. */
    val y: Int,
    /** Which horizontal side of the button the card ended up on. */
    val hSide: CardPositionCalculator.HSide,
    /** Which vertical side of the button the card ended up on. */
    val vSide: CardPositionCalculator.VSide
)
