package com.extole.mobile

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.extole.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)

        GlobalScope.launch {
            val extole = ServiceLocator.getExtole(this@MainActivity)
            runOnUiThread {
                val extoleWebView = extole.webView(webView)
                extoleWebView.load("microsite")
            }
        }
    }
}
