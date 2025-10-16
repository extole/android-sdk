package com.extole.android.sdk.impl.http

import com.extole.android.sdk.RestException
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.HttpRequestException
import org.json.JSONObject

class Endpoints(
    val accessToken: String?,
    val headers: Map<String, String> = emptyMap()
) {
    @Throws(RestException::class)
    fun executeRequest(
        httpRequest: HttpRequest,
        body: JSONObject? = null
    ): ResponseEntity<JSONObject> {
        try {
            var resultBody: String?
            if (body != null) {
                resultBody = httpRequest.send(body.toString()).body()
            } else {
                resultBody = httpRequest.body()
            }
            if (httpRequest.ok() || httpRequest.created() || httpRequest.noContent()) {
                return ResponseEntity(
                    JSONObject(resultBody.ifBlank { "{}" }),
                    httpRequest.headers(),
                    httpRequest.code()
                )
            } else {
                throw handleException(httpRequest)
            }
        } catch (e: HttpRequestException) {
            throw RestException(
                "http_request_exception", "500", "http_request_exception", e.message
                    ?: "HttpRequestException", emptyMap()
            )
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
