package com.extole.android.sdk

import android.webkit.WebView

interface ProtocolHandler {
    fun handle(view: WebView, url: String)
    fun isInterested(url: String): Boolean
}
