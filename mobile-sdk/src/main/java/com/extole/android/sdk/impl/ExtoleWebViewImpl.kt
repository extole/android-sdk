package com.extole.webview

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentSender
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.extole.android.sdk.Extole
import com.extole.android.sdk.Extole.Companion.PARTNER_SHARE_ID_PREFERENCES_KEY
import com.extole.android.sdk.ExtoleWebView
import com.extole.android.sdk.ProtocolHandler
import com.extole.android.sdk.impl.ApplicationContext
import com.extole.android.sdk.impl.ExtoleShareBroadcastReceiver
import com.extole.android.sdk.impl.JsExtoleShareImpl
import com.extole.android.sdk.impl.protocol.handlers.MailtoProtocolHandler
import com.extole.android.sdk.impl.protocol.handlers.SmsProtocolHandler
import com.extole.android.sdk.impl.protocol.handlers.TelProtocolHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class ExtoleWebViewImpl(
    private val programDomain: String,
    private val webView: WebView,
    private val context: ApplicationContext,
    private val extole: Extole,
    private val headers: Map<String, String>,
    private val queryParameters: Map<String, String>,
    private val protocolHandlers: List<ProtocolHandler> = emptyList()
) : ExtoleWebView {

    companion object {
        val DEFAULT_PROTOCOL_HANDLERS = listOf(
            SmsProtocolHandler(),
            TelProtocolHandler(),
            MailtoProtocolHandler()
        )
    }

    init {
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            JsExtoleShareImpl(
                context.getAppContext(), getPartnerShareId(),
                intentScopeExtender(), afterShareAction()
            ),
            "extoleShare"
        )
        val allProtocolHandlers = protocolHandlers.toMutableList()
        allProtocolHandlers.addAll(DEFAULT_PROTOCOL_HANDLERS)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val interestedProtocolHandlers =
                    allProtocolHandlers.filter { it.isInterested(url) }
                if (interestedProtocolHandlers.isNotEmpty()) {
                    interestedProtocolHandlers.forEach {
                        it.handle(view, url)
                    }
                    return true
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                browserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
                return true
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                extole.getLogger().error(
                    "Http error request (url=${request?.url}, " +
                        "method=${request?.method}, requestHeaders=${request?.requestHeaders}), " +
                        "error: ${errorResponse?.reasonPhrase}"
                )
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                extole.getLogger().error("SSL error: $error")
                super.onReceivedSslError(view, handler, error)
            }
        }
    }

    override fun load(zone: String) {
        val uriBuilder = Uri.Builder().scheme("https")
            .appendEncodedPath("$programDomain/zone/$zone")
        queryParameters.forEach {
            uriBuilder.appendQueryParameter(it.key, it.value)
        }
        webView.loadUrl(uriBuilder.build().toString(), headers)
    }

    private fun afterShareAction(): () -> Unit = {
        GlobalScope.launch {
            extole.getLogger().info("Sending preshare event")
            extole.sendEvent("preshare", mapOf("partner_share_id" to getPartnerShareId()))
        }
    }

    private fun intentScopeExtender(): ((intent: Intent, title: String?) -> Intent) {
        return { intent, title ->
            val pendingIntent = createPendingIntent(ExtoleShareBroadcastReceiver::class.java)
            createChooser(intent, title, pendingIntent.intentSender)
        }
    }

    private fun createChooser(target: Intent, title: CharSequence?, sender: IntentSender?): Intent {
        val intent = Intent(Intent.ACTION_CHOOSER)
        intent.putExtra(Intent.EXTRA_INTENT, target)
        if (title != null) {
            intent.putExtra(Intent.EXTRA_TITLE, title)
        }

        if (sender != null) {
            intent.putExtra(Intent.EXTRA_CHOSEN_COMPONENT_INTENT_SENDER, sender)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    fun getPartnerShareId(): String? {
        val partnerShareId =
            context.getPersistence().get(PARTNER_SHARE_ID_PREFERENCES_KEY)
        if (partnerShareId.isNullOrEmpty()) {
            extole.getLogger().debug("Partner Share id is empty for WebView")
        }
        return partnerShareId
    }

    private fun createPendingIntent(broadcastReceiver: Class<ExtoleShareBroadcastReceiver>): PendingIntent {
        return PendingIntent.getBroadcast(
            context.getAppContext(), 0,
            Intent(context.getAppContext(), broadcastReceiver),
            PendingIntent.FLAG_MUTABLE
        )
    }
}
