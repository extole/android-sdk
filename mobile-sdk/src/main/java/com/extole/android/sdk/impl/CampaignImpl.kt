package com.extole.android.sdk.impl

import android.webkit.WebView
import com.extole.android.sdk.Campaign
import com.extole.android.sdk.Event
import com.extole.android.sdk.Extole
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.ExtoleServices
import com.extole.android.sdk.ExtoleWebView
import com.extole.android.sdk.Id
import com.extole.android.sdk.RestException
import com.extole.android.sdk.SendError
import com.extole.android.sdk.Zone
import com.extole.android.sdk.randomAlphaNumericString
import com.extole.webview.ExtoleWebViewImpl

class CampaignImpl(
    private val campaignId: Id<Campaign>,
    private val zone: Zone,
    private val extole: ExtoleImpl
) : Campaign {
    private val zones: MutableMap<String, Map<String, Any?>?> = mutableMapOf()

    override fun getProgramLabel(): String {
        return extole.getLabels().joinToString(",")
    }

    override fun getId(): Id<Campaign> {
        return campaignId
    }

    @Throws(RestException::class)
    override suspend fun fetchZone(
        zoneName: String,
        fethZoneData: Map<String, Any?>
    ): Pair<Zone, Campaign> {
        var prefetchContent = zone.get(zoneName)
        fethZoneData.toMutableMap()["target"] = "campaign_id:$campaignId"
        if (prefetchContent == null) {
            if (zones.containsKey(zoneName)) {
                prefetchContent = zones[zoneName]
            } else {
                prefetchContent = fetchZone(setOf(zoneName), fethZoneData).get(zoneName)?.content
                zones[zoneName] = prefetchContent
            }
        }
        return Pair(Zone(zoneName, campaignId, prefetchContent as Map<String, Any?>?, extole), this)
    }

    override fun getServices(): ExtoleServices = extole.getServices()

    override fun getLogger(): ExtoleLogger = extole.getLogger()

    override fun getContext(): ApplicationContext = extole.getContext()

    private suspend fun fetchZone(
        zonesName: Set<String>,
        data: Map<String, Any?> = emptyMap()
    ): Zones {
        return extole.getServices().getZoneService()
            .getZones(
                zonesName,
                prepareRequestData(data),
                emptySet()
            )
    }

    private fun prepareRequestData(data: Map<String, Any?>): Map<String, Any?> {
        val requestData = mutableMapOf<String, Any?>()
        requestData.putAll(data)
        requestData["campaign_id"] = campaignId.id ?: ""
        requestData["labels"] = extole.getLabels().joinToString(",")
        return requestData
    }

    override suspend fun sendEvent(eventName: String, data: Map<String, Any?>): Id<Event> {
        try {
            val requestData = mutableMapOf<String, Any?>()
            requestData.putAll(data)
            requestData["labels"] = extole.getLabels().joinToString(",")
            requestData["target"] = "campaign_id:$campaignId"
            requestData["sandbox"] = extole.sandbox
            val requestBody = mutableMapOf<String, Any?>()
            requestBody["event_name"] = eventName
            requestBody["data"] = requestData
            return Id(
                extole.getServices().getEventsEndpoints().post(requestBody).entity.getString("id")
            )
        } catch (exception: RestException) {
            throw SendError(
                exception.uniqueId,
                exception.errorCode,
                exception.httpStatusCode,
                exception.message,
                exception.parameters
            )
        }
    }

    override suspend fun identify(email: String, data: Map<String, String>): Id<Event> {
        return extole.identify(email, data)
    }

    override fun logout() {
        extole.logout()
    }

    override suspend fun clone(
        programDomain: String?,
        appName: String?,
        sandbox: String?,
        context: ApplicationContext?,
        newLabels: Set<String>?,
        extendCurrentLabels: Set<String>,
        newData: Map<String, String>?,
        extendCurrentData: Map<String, String>,
        appData: Map<String, String>?,
        appHeaders: Map<String, String>?,
        email: String?
    ): Extole {
        return extole.clone(
            programDomain,
            appName,
            sandbox,
            context,
            newLabels,
            extendCurrentLabels,
            newData,
            extendCurrentData,
            appData,
            appHeaders,
            email
        )
    }

    override fun webView(
        webView: WebView,
        headers: Map<String, String>,
        data: Map<String, String>
    ): ExtoleWebView {
        val partnerShareId = randomAlphaNumericString()
        val mutableHeaders = headers.toMutableMap()
        mutableHeaders.put("Authorization", "Bearer ${extole.getAccessToken()}")
        val mutableData = data.toMutableMap()
        mutableData.putAll(extole.getData())
        mutableData.putAll(extole.appData)
        extole.getContext().getPersistence()
            .put(Extole.PARTNER_SHARE_ID_PREFERENCES_KEY, partnerShareId)
        return ExtoleWebViewImpl(
            extole.getProgramDomain(),
            webView,
            extole.getContext(),
            extole,
            mutableHeaders,
            mutableData,
            extole.additionalProtocolHandlers
        )
    }
}
