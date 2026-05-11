package com.extole.android.sdk.impl.http

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.FileNotFoundException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EndpointsErrorHandlingTest {

    @Test
    fun structuredErrorParsesEnvelopeAndNoCreative() {
        val body =
            """
            {"unique_id":"zr_1","http_status_code":"400","code":"no_creative","message":"Not available","parameters":{}}
            """.trimIndent()

        val ex = restExceptionFromHttpErrorResponse("400", body)

        assertEquals("zr_1", ex.uniqueId)
        assertEquals("400", ex.httpStatusCode)
        assertEquals("no_creative", ex.errorCode)
        assertEquals("Not available", ex.message)
        assertEquals(emptyMap<String, Any>(), ex.parameters)
    }

    @Test
    fun structuredErrorFillsMissingHttpStatusCodeFromWire() {
        val body =
            """
            {"unique_id":"uid","code":"rate_limited","message":"Slow down","parameters":{}}
            """.trimIndent()

        val ex = restExceptionFromHttpErrorResponse("429", body)

        assertEquals("429", ex.httpStatusCode)
        assertEquals("rate_limited", ex.errorCode)
    }

    @Test
    fun structuredErrorMissingParametersProducesEmptyParametersMap() {
        val body =
            """
            {"unique_id":"uid","http_status_code":"400","code":"no_creative","message":"x"}
            """.trimIndent()

        val ex = restExceptionFromHttpErrorResponse("400", body)
        assertEquals(emptyMap<String, Any>(), ex.parameters)
    }

    @Test
    fun emptyBodyUsesWireStatus() {
        val ex = restExceptionFromHttpErrorResponse("400", "")
        assertEquals("empty_error_body", ex.errorCode)
        assertEquals("400", ex.httpStatusCode)
        assertTrue(ex.message.contains("HTTP 400"))
    }

    @Test
    fun emptyBodyFallbackStatusWhenWireUnknown() {
        val ex = restExceptionFromHttpErrorResponse(null, "   ")
        assertEquals("400", ex.httpStatusCode)
        assertEquals("empty_error_body", ex.uniqueId)
    }

    @Test
    fun malformedJsonProducesInvalidPayloadPlaceholder() {
        val ex = restExceptionFromHttpErrorResponse("503", "<html>Error</html>")
        assertEquals("invalid_error_payload", ex.errorCode)
        assertEquals("503", ex.httpStatusCode)
        assertTrue(ex.message.contains("<html>"))
    }

    @Test
    fun jsonMissingEnvelopeProducesUnexpectedPayload() {
        val ex = restExceptionFromHttpErrorResponse(null, "{\"foo\":1}")
        assertEquals("unexpected_error_payload", ex.uniqueId)
        assertEquals("400", ex.httpStatusCode)
        assertEquals("{\"foo\":1}", ex.message)
    }

    @Test
    fun transportFailureUsesResolvedHttpStatus() {
        val cause =
            HttpRequest.HttpRequestException(
                FileNotFoundException("https://example/api/v6/zones/z")
            )
        val ex = restExceptionFromTransportFailure("400", cause)
        assertEquals("400", ex.httpStatusCode)
        assertEquals("http_request_exception", ex.errorCode)
        assertTrue(ex.message.contains("FileNotFoundException"))
        assertTrue(ex.message.contains("https://example/api/v6/zones/z"))
    }

    @Test
    fun transportFailureWithoutStatusUsesGatewayFallback() {
        val cause =
            HttpRequest.HttpRequestException(IOException("Connection reset"))

        val ex = restExceptionFromTransportFailure(null, cause)
        assertEquals("502", ex.httpStatusCode)
        assertEquals("http_request_exception", ex.uniqueId)
        assertTrue(ex.message.contains("IOException"))
    }

    @Test
    fun readableTransportMessageAddsExceptionTypeWhenMissingFromDetail() {
        val url = "https://program.example/api/v6/zones/marketing_experience"
        val ex =
            readableTransportMessage(
                HttpRequest.HttpRequestException(FileNotFoundException(url))
            )
        assertTrue(ex.contains("FileNotFoundException"))
        assertTrue(ex.contains(url))
    }
}
