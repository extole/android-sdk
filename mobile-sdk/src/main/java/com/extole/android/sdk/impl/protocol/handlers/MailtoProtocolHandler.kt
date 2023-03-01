package com.extole.android.sdk.impl.protocol.handlers

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import com.extole.android.sdk.ProtocolHandler

class MailtoProtocolHandler : ProtocolHandler {
    companion object {
        private const val PROTOCOL = "mailto"
    }

    override fun handle(view: WebView, url: String) {
        val schemeSpecificPart = url.replace("$PROTOCOL:", "")
        view.getContext().startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.fromParts(PROTOCOL, schemeSpecificPart, null)
            )
        )
    }

    override fun isInterested(url: String): Boolean = url.startsWith(PROTOCOL)
}
