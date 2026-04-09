package com.extole.android.sdk.impl

import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException

internal object NetworkConnectivityErrorFilter {
    fun shouldSuppressRemoteUpload(exception: Throwable): Boolean =
        generateSequence(exception as Throwable?) { it.cause }
            .any(::isSuppressibleConnectivityCause)

    private fun isSuppressibleConnectivityCause(exception: Throwable): Boolean {
        return exception is SocketTimeoutException ||
            exception is NoRouteToHostException ||
            exception is SocketException &&
            exception.message?.contains("network is unreachable", ignoreCase = true) == true
    }
}
