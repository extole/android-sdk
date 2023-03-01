package com.extole.android.sdk.impl.http

import com.extole.android.sdk.RestException
import com.extole.android.sdk.impl.ResponseEntity
import org.json.JSONObject

class Endpoints(
    val accessToken: String?,
    val headers: Map<String, String> = emptyMap()
) {
    fun handleResponse(httpRequest: HttpRequest): ResponseEntity<JSONObject> {
        if (httpRequest.ok() || httpRequest.created() || httpRequest.noContent()) {
            return ResponseEntity(
                JSONObject(httpRequest.body()),
                httpRequest.headers(),
                httpRequest.code()
            )
        } else {
            throw handleException(httpRequest)
        }
    }

    fun createHttpRequest(baseUrl: String, requestMethod: String): HttpRequest {
        return HttpRequest(baseUrl, requestMethod)
            .authorization(accessToken)
            .acceptJson()
            .contentType(HttpRequest.CONTENT_TYPE_JSON)
            .headers(headers)
    }

    private fun handleException(httpRequest: HttpRequest): RestException {
        val responseBody = JSONObject(httpRequest.body())
        return RestException(
            responseBody.getString("unique_id"),
            responseBody.getString("http_status_code"),
            responseBody.getString("code"),
            responseBody.getString("message"),
            toMap(responseBody.getJSONObject("parameters"))
        )
    }
}
