package com.extole.android.sdk.impl.http

import com.extole.android.sdk.RestException
import com.extole.android.sdk.impl.ResponseEntity
import com.extole.android.sdk.impl.http.HttpRequest.HttpRequestException
import org.json.JSONException
import org.json.JSONObject

private const val HTTP_STATUS_MIN = 100
private const val HTTP_STATUS_MAX = 599
private const val ERROR_BODY_PREVIEW_LENGTH = 512
private const val FALLBACK_TRANSPORT_ERROR_STATUS = "502"
private const val FALLBACK_APPLICATION_ERROR_STATUS = "400"

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
            val responseBody =
                if (body != null) httpRequest.send(body.toString()).body()
                else httpRequest.body()

            when {
                httpRequest.ok() || httpRequest.created() || httpRequest.noContent() ->
                    return ResponseEntity(
                        JSONObject(responseBody.ifBlank { "{}" }),
                        httpRequest.headers(),
                        httpRequest.code()
                    )

                else ->
                    throw restExceptionFromHttpErrorResponse(
                        resolveHttpStatusCode(httpRequest),
                        responseBody
                    )
            }
        } catch (e: HttpRequestException) {
            throw restExceptionFromTransportFailure(resolveHttpStatusCode(httpRequest), e)
        }
    }

    fun createHttpRequest(baseUrl: String, requestMethod: String): HttpRequest {
        return HttpRequest(baseUrl, requestMethod)
            .authorization(accessToken)
            .acceptJson()
            .contentType(HttpRequest.CONTENT_TYPE_JSON)
            .headers(headers)
    }

    private fun resolveHttpStatusCode(httpRequest: HttpRequest): String? =
        try {
            val code = httpRequest.code()
            if (code in HTTP_STATUS_MIN..HTTP_STATUS_MAX) code.toString() else null
        } catch (_: HttpRequestException) {
            null
        }
}

internal fun restExceptionFromHttpErrorResponse(
    statusFromWire: String?,
    rawBody: String
): RestException {
    val trimmedBody = rawBody.trim()
    if (trimmedBody.isEmpty()) {
        return RestException(
            uniqueId = "empty_error_body",
            httpStatusCode = statusFromWire ?: FALLBACK_APPLICATION_ERROR_STATUS,
            errorCode = "empty_error_body",
            message =
                if (statusFromWire != null) {
                    "Empty response body for HTTP $statusFromWire"
                } else {
                    "Empty error response body"
                },
            parameters = emptyMap()
        )
    }

    return try {
        val json = JSONObject(trimmedBody)
        if (isStructuredRestError(json)) {
            val statusFromPayload = json.optString("http_status_code").trim()
            RestException(
                json.getString("unique_id"),
                statusFromPayload.ifBlank { statusFromWire ?: FALLBACK_APPLICATION_ERROR_STATUS },
                json.getString("code"),
                json.getString("message"),
                toMap(json.optJSONObject("parameters") ?: JSONObject())
            )
        } else {
            RestException(
                uniqueId = "unexpected_error_payload",
                httpStatusCode = statusFromWire ?: FALLBACK_APPLICATION_ERROR_STATUS,
                errorCode = "unexpected_error_payload",
                message = trimmedBody.take(ERROR_BODY_PREVIEW_LENGTH),
                parameters = emptyMap()
            )
        }
    } catch (_: JSONException) {
        RestException(
            uniqueId = "invalid_error_payload",
            httpStatusCode = statusFromWire ?: FALLBACK_APPLICATION_ERROR_STATUS,
            errorCode = "invalid_error_payload",
            message = trimmedBody.take(ERROR_BODY_PREVIEW_LENGTH),
            parameters = emptyMap()
        )
    }
}

internal fun restExceptionFromTransportFailure(
    resolvedHttpStatusToken: String?,
    cause: HttpRequestException
): RestException =
    RestException(
        uniqueId = "http_request_exception",
        httpStatusCode = resolvedHttpStatusToken ?: FALLBACK_TRANSPORT_ERROR_STATUS,
        errorCode = "http_request_exception",
        message = readableTransportMessage(cause),
        parameters = emptyMap()
    )

internal fun readableTransportMessage(e: HttpRequestException): String {
    val cause = e.cause
    val detail =
        cause?.message?.takeIf { it.isNotBlank() }
            ?: e.message?.takeIf { it.isNotBlank() }
            ?: "HttpRequestException"
    if (cause == null) return detail
    val type = cause.javaClass.simpleName
    val fqcnPrefix = "${cause.javaClass.name}:"
    return if (detail.startsWith(type) || detail.startsWith(fqcnPrefix)) detail else "$type: $detail"
}

private fun isStructuredRestError(body: JSONObject): Boolean =
    body.has("unique_id") && body.has("code") && body.has("message")
