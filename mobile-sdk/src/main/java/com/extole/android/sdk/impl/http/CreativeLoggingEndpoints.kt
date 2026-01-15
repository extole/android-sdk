package com.extole.android.sdk.impl.http

import android.net.Uri
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_POST
import org.json.JSONObject

class CreativeLoggingEndpoints(val programDomain: String, val accessToken: String?) {

    private val endpoints = Endpoints(accessToken)
    private val baseUrl = Uri.Builder().scheme("https")
        .authority(programDomain)
        .appendEncodedPath("api/v4/debug/logs")
        .build().toString()

    fun create(level: String, message: String): ResponseEntity<JSONObject> {
        val jsonObject = JSONObject()
        val httpRequest = endpoints.createHttpRequest(baseUrl, METHOD_POST)
        jsonObject.put("level", level)
        jsonObject.put("message", message)
        return endpoints.executeRequest(httpRequest, jsonObject)
    }
}
