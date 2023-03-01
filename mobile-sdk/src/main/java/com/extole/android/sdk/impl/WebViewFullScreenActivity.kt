package com.extole.android.sdk.impl

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.extole.android.sdk.impl.app.App
import com.extole.mobile.sdk.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WebViewFullScreenActivity : Activity() {

    private var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.com_extole_activity_webview)

        val extras = intent.extras
        val extoleZoneName = extras!!.getString("extole_zone_name").orEmpty()
        val data = extras.keySet()
            .mapNotNull { it to extras.getString(it) }
            .associateBy({ it.first!! }, { it.second!! })
        webView = findViewById<WebView>(R.id.webview)
        webView?.let {
            GlobalScope.launch {
                runOnUiThread {
                    val extoleWebView = App.extole.webView(webView!!, data = data)
                    extoleWebView.load(extoleZoneName)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            finish()
        }
    }
}
