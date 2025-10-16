package com.extole.android.sdk.impl.http

import android.net.Uri
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_GET
import org.json.JSONObject

class MeRewardEndpoints(
    val programDomain: String,
    val accessToken: String?,
    val headers: Map<String, String>
) {

    private val endpoints = Endpoints(accessToken, headers)
    private val baseUrl = Uri.Builder().scheme("https")
        .authority(programDomain)
        .appendEncodedPath("api/v4/me/rewards/status")
        .build().toString()

    fun getRewardStatus(
        pollingId: String?,
        rewardName: String?,
        partnerEventId: String?
    ): ResponseEntity<JSONObject> {
        val params = mutableMapOf<String, String?>()
        pollingId?.let { params.put("polling_id", it) }
        rewardName?.let { params.put("reward_name", it) }
        partnerEventId?.let { params.put("partner_event_id", it) }

        val requestUrl = HttpRequest.encode(HttpRequest.append(baseUrl, params))
        val httpRequest = endpoints.createHttpRequest(requestUrl, METHOD_GET)
        return endpoints.executeRequest(httpRequest)
    }
}
