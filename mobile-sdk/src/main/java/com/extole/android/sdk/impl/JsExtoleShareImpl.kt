package com.extole.android.sdk.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import com.extole.android.sdk.JsExtoleShare
import org.json.JSONObject

class JsExtoleShareImpl(
    private val context: Context,
    private val partnerShareId: String?,
    private val intentScopeExtender: (intent: Intent, title: String?) -> Intent = { intent, _ -> intent },
    private val afterShare: () -> Unit = {}
) : JsExtoleShare {

    @JavascriptInterface
    override fun share(jsonData: String?) {
        val jsonObject = JSONObject(jsonData.orEmpty())
        val shareUri = Uri.parse(jsonObject.getString("url"))
            .buildUpon()
            .appendQueryParameter("partner_share_id", partnerShareId)
            .build()

        val title = jsonObject.getString("title")
        val shareText = jsonObject.getString("text") + " " + shareUri.toString()

        val share = intentScopeExtender(defaultShareIntentCreator(title, shareText), title)
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(share)
        afterShare()
    }

    private fun defaultShareIntentCreator(title: String?, shareText: String): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return shareIntent
    }
}
