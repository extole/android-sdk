package com.extole.android.sdk.impl.http

import android.net.Uri
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_GET
import com.extole.android.sdk.impl.http.HttpRequest.METHOD_POST
import org.json.JSONObject

class AuthorizationEndpoints(
    val programDomain: String,
    val accessToken: String?,
    val headers: Map<String, String>
) {
    private val endpoints = Endpoints(accessToken, headers)
    private val baseUrl = Uri.Builder().scheme("https")
        .authority(programDomain)
        .appendEncodedPath("api/v5/token")
        .build().toString()

    fun getTokenDetails(queryParams: Map<String?, Any?>?): ResponseEntity<JSONObject> {
        val requestUrl = HttpRequest.encode(HttpRequest.append(baseUrl, queryParams))
        val httpRequest = endpoints.createHttpRequest(requestUrl, METHOD_GET)
        return endpoints.handleResponse(httpRequest)
    }

    fun createToken(email: String?): ResponseEntity<JSONObject> {
        val body = JSONObject()
        val httpRequest = endpoints.createHttpRequest(baseUrl, METHOD_POST)
        email?.let { body.put("email", it) }
        httpRequest.send(body.toString())
        return endpoints.handleResponse(httpRequest)
    }
}
