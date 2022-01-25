package com.extole.androidsdk

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView: WebView = findViewById(R.id.webview)

        GlobalScope.launch {
            val extole = ServiceLocator.getExtole(this@WebViewActivity)
            runOnUiThread {
                val extoleWebView = extole.webView(webView)
                extoleWebView.load("microsite")
            }
        }
    }
}
