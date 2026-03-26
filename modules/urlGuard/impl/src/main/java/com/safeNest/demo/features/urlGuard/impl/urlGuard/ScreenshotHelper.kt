package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Handles screenshot capture and saving to the Pictures/SafeNest gallery folder.
 *
 * [saveToGallery] is safe to call from any thread (runs on Dispatchers.IO).
 *
 * Set [onScreenshotSaved] to receive the gallery [Uri] (or null on failure) after
 * each save. The callback is always delivered on the main thread.
 */
class ScreenshotHelper(
    private val context: Context,
    private val scope: CoroutineScope,
    private val handler: Handler
) {

    /**
     * Invoked on the main thread after every save attempt.
     * Receives the [Uri] of the saved image, or null if saving failed.
     */
    var onScreenshotSaved: ((Uri?) -> Unit)? = null

    // ── MediaProjection token (API < 30 path) ─────────────────────────────────

    private var mediaProjectionResultCode: Int    = Activity.RESULT_CANCELED
    private var mediaProjectionData      : Intent? = null

    /**
     * Store the MediaProjection token obtained from [ScreenCapturePermissionActivity].
     * Must be called before [takeViaMediaProjection].
     */
    fun setProjectionData(resultCode: Int, data: Intent) {
        mediaProjectionResultCode = resultCode
        mediaProjectionData       = data
    }

    // ── API 30+ callbacks (the service calls takeScreenshot() itself) ─────────

    @RequiresApi(Build.VERSION_CODES.R)
    fun onA11yScreenshotSuccess(result: AccessibilityService.ScreenshotResult) {
        val hardware = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
        result.hardwareBuffer.close()
        val bitmap = hardware?.copy(Bitmap.Config.ARGB_8888, false)
        hardware?.recycle()
        if (bitmap != null) {
            scope.launch(Dispatchers.IO) {
                val uri = saveToGallery(bitmap)
                handler.post { onScreenshotSaved?.invoke(uri) }
            }
        } else {
            Log.e(TAG, "onA11yScreenshotSuccess: bitmap conversion failed")
            handler.post { onScreenshotSaved?.invoke(null) }
        }
    }

    fun onA11yScreenshotFailure(errorCode: Int) {
        Log.e(TAG, "A11y screenshot failed: errorCode=$errorCode")
        handler.post { onScreenshotSaved?.invoke(null) }
    }

    // ── API 24-29: MediaProjection path ───────────────────────────────────────

    /**
     * Captures a screenshot using the MediaProjection token stored via [setProjectionData].
     * No-op if capture permission was never granted.
     */
    fun takeViaMediaProjection() {
        val data = mediaProjectionData
        if (mediaProjectionResultCode == Activity.RESULT_CANCELED || data == null) {
            Log.w(TAG, "takeViaMediaProjection: capture permission not granted")
            handler.post { onScreenshotSaved?.invoke(null) }
            return
        }

        val metrics    = context.resources.displayMetrics
        val width      = metrics.widthPixels
        val height     = metrics.heightPixels
        val density    = metrics.densityDpi

        val projMgr    = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = try {
            projMgr.getMediaProjection(mediaProjectionResultCode, data)
        } catch (e: Exception) {
            Log.e(TAG, "getMediaProjection failed: ${e.message}")
            handler.post { onScreenshotSaved?.invoke(null) }
            return
        }

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val vDisplay    = projection?.createVirtualDisplay(
            "safenest_screenshot", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )

        handler.postDelayed({
            val image = imageReader.acquireLatestImage()
            if (image != null) {
                val plane      = image.planes[0]
                val rowPadding = plane.rowStride - plane.pixelStride * width
                val raw        = Bitmap.createBitmap(
                    width + rowPadding / plane.pixelStride, height, Bitmap.Config.ARGB_8888
                )
                raw.copyPixelsFromBuffer(plane.buffer)
                image.close()
                vDisplay?.release()
                projection?.stop()
                imageReader.close()
                val bitmap = Bitmap.createBitmap(raw, 0, 0, width, height)
                raw.recycle()
                scope.launch(Dispatchers.IO) {
                    val uri = saveToGallery(bitmap)
                    handler.post { onScreenshotSaved?.invoke(uri) }
                }
            } else {
                vDisplay?.release()
                projection?.stop()
                imageReader.close()
                Log.e(TAG, "takeViaMediaProjection: no image captured")
                handler.post { onScreenshotSaved?.invoke(null) }
            }
        }, 300L)
    }

    // ── Gallery save (IO thread) ──────────────────────────────────────────────

    /**
     * Saves [bitmap] to the Pictures/SafeNest gallery folder.
     *
     * @return the [Uri] of the saved image, or null on failure.
     */
    fun saveToGallery(bitmap: Bitmap): Uri? {
        val filename = "SafeNest_${System.currentTimeMillis()}.png"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/SafeNest"
                    )
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                bitmap.recycle()
                Log.i(TAG, "Screenshot saved (Q+): $filename uri=$uri")
                uri
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "SafeNest"
                )
                dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)
                    )
                )
                bitmap.recycle()
                val uri = Uri.fromFile(file)
                Log.i(TAG, "Screenshot saved (legacy): $filename uri=$uri")
                uri
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveToGallery failed: ${e.message}", e)
            bitmap.recycle()
            null
        }
    }

    companion object {
        private const val TAG = "ScreenshotHelper"
    }
}
