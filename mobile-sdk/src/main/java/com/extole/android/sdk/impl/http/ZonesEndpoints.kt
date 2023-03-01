package com.extole.android.sdk.impl.http

import android.net.Uri
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_POST
import org.json.JSONObject

class ZonesEndpoints(
    val programDomain: String,
    val accessToken: String?,
    val headers: Map<String, String>
) {

    private val endpoints = Endpoints(accessToken, headers)
    private val baseUrl = Uri.Builder().scheme("https")
        .authority(programDomain)
        .appendEncodedPath("api/v6/zones")
        .build().toString()

    fun render(
        eventName: String?,
        body: Map<String, Any?>
    ): ResponseEntity<JSONObject> {
        val jsonObject = JSONObject(body.toMutableMap())
        val urlBuilder = Uri.parse(baseUrl)
            .buildUpon()
        val httpRequest =
            endpoints.createHttpRequest("${urlBuilder.build()}/$eventName", METHOD_POST)
        val response = httpRequest.send(jsonObject.toString())
        return endpoints.handleResponse(response)
    }
}
