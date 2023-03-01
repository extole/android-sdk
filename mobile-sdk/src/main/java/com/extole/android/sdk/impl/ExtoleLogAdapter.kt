package com.extole.android.sdk.impl

import com.extole.android.sdk.LogLevel
import com.orhanobut.logger.LogAdapter

open class ExtoleLogAdapter(
    val programDomain: String,
    val accessToken: String?,
    val context: ExtoleContext,
    var logLevel: LogLevel = LogLevel.ERROR,
    var logSender: ((priority: Int, tag: String?, message: String) -> Unit)
) : LogAdapter {

    override fun isLoggable(priority: Int, tag: String?): Boolean {
        return LogLevel.DISABLE != logLevel && priority >= logLevel.priority
    }

    override fun log(priority: Int, tag: String?, message: String) {
        logSender(priority, tag, message)
    }
}
