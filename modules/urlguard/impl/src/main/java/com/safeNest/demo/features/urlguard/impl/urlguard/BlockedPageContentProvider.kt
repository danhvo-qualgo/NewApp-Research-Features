package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Serves the "blocked" page as HTML at content://com.safenest.urlGuard/blocked.
 * The browser loads this URL in its own view, so the block UI appears inside the browser.
 */
class BlockedPageContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate")
        return true
    }

    override fun getType(uri: Uri): String? {
        val match = sUriMatcher.match(uri)
        Log.d(TAG, "getType uri=$uri match=$match")
        if (match == CODE_BLOCKED) return "text/html"
        return null
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Log.d(TAG, "openFile uri=$uri mode=$mode")
        if (sUriMatcher.match(uri) != CODE_BLOCKED) {
            Log.w(TAG, "openFile: uri did not match blocked path")
            return null
        }
        if (mode != "r") {
            Log.w(TAG, "openFile: mode is not read")
            return null
        }
        grantReadPermissionToCaller(uri)
        val raw = uri.getQueryParameter(QUERY_BLOCKED_URL)
        val blockedUrl = if (!raw.isNullOrBlank()) {
            try {
                URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
            } catch (e: Exception) {
                Log.w(TAG, "openFile: decode url failed", e)
                raw
            }
        } else ""
        val html = buildBlockedPageHtmlStatic(blockedUrl)
        val pfd = createTempHtmlFile(html)
        Log.d(TAG, "openFile: returning pfd=${pfd != null} htmlLen=${html.length}")
        return pfd
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    /**
     * Grant the calling app (e.g. Chrome) read permission for this URI so it can load content:// in its WebView.
     */
    private fun grantReadPermissionToCaller(uri: Uri) {
        val ctx = context ?: return
        val callingUid = Binder.getCallingUid()
        val pkgs = ctx.packageManager.getPackagesForUid(callingUid)
        val pkg = pkgs?.firstOrNull()
        Log.d(TAG, "grantReadPermissionToCaller callingUid=$callingUid pkg=$pkg pkgs=$pkgs")
        if (pkg != null) {
            ctx.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Log.d(TAG, "grantReadPermissionToCaller: granted READ to $pkg for $uri")
        } else {
            Log.w(TAG, "grantReadPermissionToCaller: could not resolve package for uid $callingUid")
        }
    }

    private fun buildBlockedPageHtml(blockedUrl: String): String =
        buildBlockedPageHtmlStatic(blockedUrl)

    companion object {
        /** Same block page HTML as a static function so the service can build a data: URL without using the ContentProvider. */
        fun buildBlockedPageHtmlStatic(blockedUrl: String): String {
            val displayUrl = if (blockedUrl.length > 80) blockedUrl.take(77) + "…" else blockedUrl
            val escapedUrl = displayUrl
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Blocked – SafeNest UrlGuard</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: system-ui, -apple-system, sans-serif;
                            background: #ffdad6;
                            color: #410002;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            padding: 24px;
                            text-align: center;
                        }
                        .icon { font-size: 64px; margin-bottom: 16px; }
                        h1 { font-size: 1.75rem; margin: 0 0 8px; }
                        .sub { font-size: 1rem; opacity: 0.9; margin-bottom: 16px; }
                        .url { font-size: 0.875rem; word-break: break-all; opacity: 0.8; max-width: 100%; }
                    </style>
                </head>
                <body>
                    <div class="icon" aria-hidden="true">🛡️</div>
                    <h1>Blocked - SafeNest</h1>
                    <p class="sub">This site was blocked for your safety.</p>
                    ${if (displayUrl.isNotBlank()) "<p class=\"url\">$escapedUrl</p>" else ""}
                </body>
                </html>
            """.trimIndent()
        }

        private const val TAG = "BlockedPageProvider"
        private const val AUTHORITY = "com.safenest.urlGuard"
        private const val PATH_BLOCKED = "blocked"
        private const val CODE_BLOCKED = 1
        private const val QUERY_BLOCKED_URL = "url"

        val BLOCKED_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_BLOCKED")

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_BLOCKED, CODE_BLOCKED)
        }

        fun buildBlockedUri(blockedUrl: String): Uri {
            return BLOCKED_URI.buildUpon()
                .appendQueryParameter(QUERY_BLOCKED_URL, blockedUrl)
                .build()
        }
    }

    private fun createTempHtmlFile(html: String): ParcelFileDescriptor? {
        return try {
            val cacheDir = context?.cacheDir
            if (cacheDir == null) {
                Log.e(TAG, "createTempHtmlFile: no cache dir")
                return null
            }
            val file = File(cacheDir, "blocked_${System.currentTimeMillis()}.html")
            FileOutputStream(file).use { out ->
                OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
                    writer.write(html)
                }
            }
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            Log.d(TAG, "createTempHtmlFile: file=${file.absolutePath} size=${file.length()}")
            pfd
        } catch (e: Exception) {
            Log.e(TAG, "createTempHtmlFile failed", e)
            null
        }
    }
}
