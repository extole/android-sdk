package com.extole.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.extole.R
import io.branch.referral.Branch
import io.branch.referral.Branch.BranchReferralInitListener
import io.branch.referral.BranchError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder

class DeeplinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink)
    }

    override fun onStart() {
        super.onStart()
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener)
            .withData(if (intent != null) intent.data else null).init()
    }

    override fun onResume() {
        super.onResume()
        intent.putExtra("branch_force_new_session", true)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra("branch_force_new_session") && intent.getBooleanExtra(
                "branch_force_new_session",
                true
            )
        ) {
            Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit()
        }
    }

    private val branchReferralInitListener =
        BranchReferralInitListener { linkProperties, error ->
            GlobalScope.launch {
                val deepLinkData = toMap(linkProperties)
                val extole = ServiceLocator.getExtole(this@DeeplinkActivity)
                extole.getLogger().info("Producing deeplink event: $deepLinkData")
                extole.sendEvent("deeplink", deepLinkData)
            }

            handleError(error, linkProperties)
        }

    private fun handleError(
        error: BranchError?,
        linkProperties: JSONObject?
    ) {
        if (error != null) {
            GlobalScope.launch {
                ServiceLocator.getExtole(this@DeeplinkActivity).getLogger()
                    .error("Deeplink error: ${error.message}")
            }
            findViewById<TextView>(R.id.deeplink_text)
                .text = error.message
        } else {
            GlobalScope.launch {
                ServiceLocator.getExtole(this@DeeplinkActivity).getLogger()
                    .info("Deeplink detected: $linkProperties")
            }
            findViewById<TextView>(R.id.deeplink_text)
                .text = linkProperties.toString()
        }
    }

    private fun toMap(linkProperties: JSONObject?): MutableMap<String, String> {
        val deepLinkData = mutableMapOf<String, String>()
        linkProperties?.keys()?.forEach {
            deepLinkData[it] = linkProperties.getString(it)
            try {
                val url: URL? = URL(
                    linkProperties.getString(it).replace("mobile-monitor", "http")
                )
                if (url != null && url.query != null) {
                    deepLinkData.putAll(splitQuery(url))
                }
            } catch (e: MalformedURLException) {
                // do nothing
            }
        }
        return deepLinkData
    }

    private fun splitQuery(url: URL): Map<String, String> {
        val queryPairs: MutableMap<String, String> = LinkedHashMap()
        val query: String = url.getQuery()
        val pairs = query.split("&").toTypedArray()
        for (pair in pairs) {
            val index = pair.indexOf("=")
            queryPairs[URLDecoder.decode(pair.substring(0, index), "UTF-8")] =
                URLDecoder.decode(pair.substring(index + 1), "UTF-8")
        }
        return queryPairs
    }
}
