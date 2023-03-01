package com.extole.android.sdk.impl.http

import android.net.Uri
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_POST
import org.json.JSONObject

class EventEndpoints(
    val programDomain: String,
    val accessToken: String?,
    val headers: Map<String, String>
) {
    private val endpoints = Endpoints(accessToken, headers)
    private val baseUrl = Uri.Builder().scheme("https")
        .authority(programDomain)
        .appendEncodedPath("/events")
        .build().toString()

    fun post(data: Map<String, Any?>): ResponseEntity<JSONObject> {
        val jsonObject = JSONObject(data.toMutableMap())
        val httpRequest = endpoints.createHttpRequest(baseUrl, METHOD_POST)
        val response = httpRequest.send(jsonObject.toString())
        return endpoints.handleResponse(response)
    }
}
