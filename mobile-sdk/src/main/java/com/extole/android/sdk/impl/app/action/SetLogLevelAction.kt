package com.extole.android.sdk.impl.app.action

import com.extole.android.sdk.Action
import com.extole.android.sdk.Action.ActionType.SET_LOG_LEVEL
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.LogLevel
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

data class SetLogLevelAction(@SerializedName("log_level") val logLevel: String) : Action {

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        val extoleLogger = ExtoleLogger.builder()
            .withProgramDomain(extole.getProgramDomain())
            .withAccessToken(extole.getAccessToken())
            .withLogLevel(toLogLevel(logLevel))
            .build()

        extole.setLogger(extoleLogger)
    }

    private fun toLogLevel(logLevel: String): LogLevel {
        return when (logLevel) {
            "DISABLE" -> LogLevel.DISABLE
            "ERROR" -> LogLevel.ERROR
            "WARN" -> LogLevel.WARN
            "INFO" -> LogLevel.INFO
            "DEBUG" -> LogLevel.DEBUG
            else -> LogLevel.ERROR
        }
    }

    override fun getType(): Action.ActionType = SET_LOG_LEVEL

    override fun toString(): String {
        return "Action: ${getType()}, logLevel: $logLevel"
    }
}
