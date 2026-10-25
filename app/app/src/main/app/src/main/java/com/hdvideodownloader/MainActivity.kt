
package com.hdvideodownloader

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var browser: WebView
    private lateinit var address: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }

        address = EditText(this).apply {
            hint = "Enter website or video URL"
            setSingleLine(true)
        }

        val open = Button(this).apply {
            text = "Open Website"
            setOnClickListener {
                var url = address.text.toString().trim()
                if (url.isNotEmpty()) {
                    if (!url.startsWith("http://") &&
                        !url.startsWith("https://")) {
                        url = "https://$url"
                    }
                    browser.loadUrl(url)
                }
            }
        }

        val download = Button(this).apply {
            text = "Download Direct Video URL"
            setOnClickListener { downloadVideo() }
        }

        browser = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            setDownloadListener { url, _, disposition, mime, _ ->
                startDownload(url, disposition, mime)
            }
        }

        layout.addView(address)
        layout.addView(open)
        layout.addView(download)
        layout.addView(
            browser,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(layout)
        browser.loadUrl("https://www.google.com")
    }

    private fun downloadVideo() {
        val input = EditText(this).apply {
            hint = "https://example.com/video.mp4"
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Direct video URL")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Download") { _, _ ->
                val url = input.text.toString().trim()
                if (url.startsWith("https://") ||
                    url.startsWith("http://")) {
                    startDownload(url, null, null)
                } else {
                    Toast.makeText(
                        this, "Enter a valid video URL",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.show()
    }

    private fun startDownload(
        url: String,
        disposition: String?,
        mime: String?
    ) {
        try {
            val fileName = URLUtil.guessFileName(
                url, disposition, mime
            )
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setNotificationVisibility(
                    DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

            val manager = getSystemService(
                Context.DOWNLOAD_SERVICE
            ) as DownloadManager

            manager.enqueue(request)

            Toast.makeText(
                this, "Download started",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this, "Download failed",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
