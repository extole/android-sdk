package com.extole.android.sdk.impl

import com.extole.android.sdk.LogLevel
import com.extole.android.sdk.RestException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkConnectivityErrorFilterTest {
    @Test
    fun testTimeoutErrorsAreSuppressed() {
        val exception = RestException(
            "unique-id",
            "500",
            "COMMUNICATION_ERROR",
            "Timeout while sending request",
            cause = SocketTimeoutException("timeout")
        )

        assertTrue(NetworkConnectivityErrorFilter.shouldSuppressRemoteUpload(exception))
    }

    @Test
    fun testUnknownHostErrorsAreStillReported() {
        val exception = RestException(
            "unique-id",
            "500",
            "COMMUNICATION_ERROR",
            "Unable to resolve host",
            cause = UnknownHostException("invalid-domain.extole.io")
        )

        assertFalse(NetworkConnectivityErrorFilter.shouldSuppressRemoteUpload(exception))
    }

    @Test
    fun testSuppressedRemoteUploadDoesNotInvokeRemoteLogAdapter() {
        var didSendRemoteLog = false
        val logAdapter = ExtoleLogAdapter(
            "program-domain",
            null,
            ExtoleContext(),
            LogLevel.ERROR
        ) { _, _, _ ->
            didSendRemoteLog = true
        }

        RemoteLogUploadContext.withSuppressedRemoteUpload(true) {
            logAdapter.log(6, "Extole", "Suppressed connectivity error")
        }

        assertFalse(didSendRemoteLog)
    }
}
