package com.safeNest.demo.urlguard.impl.urlguard

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

class FormWarningPageContentProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? =
        if (sUriMatcher.match(uri) == CODE_FORM_WARNING) "text/html" else null

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if (sUriMatcher.match(uri) != CODE_FORM_WARNING || mode != "r") return null
        grantReadPermissionToCaller(uri)

        val rawUrl = uri.getQueryParameter(QUERY_URL) ?: ""
        val rawFields = uri.getQueryParameter(QUERY_FIELDS) ?: ""

        val originalUrl = decode(rawUrl)
        val fields = decode(rawFields).split(",").filter { it.isNotBlank() }

        val html = buildWarningHtml(originalUrl, fields)
        return createTempHtmlFile(html)
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    private fun grantReadPermissionToCaller(uri: Uri) {
        val ctx = context ?: return
        val pkg =
            ctx.packageManager.getPackagesForUid(Binder.getCallingUid())?.firstOrNull() ?: return
        ctx.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun decode(raw: String): String = try {
        URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
    } catch (_: Exception) {
        raw
    }

    private fun createTempHtmlFile(html: String): ParcelFileDescriptor? = try {
        val file = File(context!!.cacheDir, "form_warning_${System.currentTimeMillis()}.html")
        FileOutputStream(file).use { out ->
            OutputStreamWriter(out, StandardCharsets.UTF_8).use { it.write(html) }
        }
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    } catch (e: Exception) {
        Log.e(TAG, "createTempHtmlFile failed", e)
        null
    }

    companion object {
        private const val TAG = "FormWarningProvider"
        private const val AUTHORITY = "com.safenest.urlguard.warning"
        private const val PATH = "form-warning"
        private const val CODE_FORM_WARNING = 1
        const val QUERY_URL = "url"
        const val QUERY_FIELDS = "fields"

        val BASE_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH, CODE_FORM_WARNING)
        }

        fun buildWarningUri(originalUrl: String, fields: List<String>): Uri =
            BASE_URI.buildUpon()
                .appendQueryParameter(QUERY_URL, originalUrl)
                .appendQueryParameter(QUERY_FIELDS, fields.joinToString(","))
                .build()

        fun buildWarningHtml(originalUrl: String, fields: List<String>): String {
            val domain = Uri.parse(originalUrl).host ?: originalUrl
            val fieldsHtml = if (fields.isEmpty()) "Sensitive fields detected"
            else fields.joinToString(" &bull; ") {
                it.replace("-", " ")
                    .replaceFirstChar { c -> c.uppercase() }
            }
            val safeUrl = originalUrl.replace("\"", "&quot;")
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Warning – SafeNest UrlGuard</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0; min-height: 100vh;
                            font-family: system-ui, -apple-system, sans-serif;
                            background: #fff8e1; color: #3e2723;
                            display: flex; flex-direction: column;
                            align-items: center; justify-content: center;
                            padding: 24px; text-align: center;
                        }
                        .icon  { font-size: 64px; margin-bottom: 16px; }
                        h1    { font-size: 1.5rem; margin: 0 0 8px; }
                        .domain { font-size: 0.875rem; opacity: 0.7; margin-bottom: 12px; }
                        .fields { font-size: 0.9rem; background: #ffe082;
                                  border-radius: 8px; padding: 8px 16px;
                                  margin-bottom: 24px; max-width: 100%; }
                        .buttons { display: flex; gap: 12px; flex-wrap: wrap;
                                   justify-content: center; }
                        button { padding: 12px 24px; border: none; border-radius: 8px;
                                 font-size: 1rem; cursor: pointer; }
                        .btn-back { background: #616161; color: #fff; }
                        .btn-continue { background: #c62828; color: #fff; }
                    </style>
                </head>
                <body>
                    <div class="icon">⚠️</div>
                    <h1>Personal Data Alert</h1>
                    <p class="domain">$domain</p>
                    <p>This page is requesting personal information:</p>
                    <div class="fields">$fieldsHtml</div>
                    <div class="buttons">
                        <button class="btn-back" onclick="goBack()">Exit</button>
                        <button class="btn-continue" onclick="window.location.href='$safeUrl'">Continue Anyway</button>
                    </div>
                </body>
                <script>
                    function goBack() {
                        window.location.href='about:blank'
                    }
                    </script>
                </html>
            """.trimIndent()
        }
    }
}