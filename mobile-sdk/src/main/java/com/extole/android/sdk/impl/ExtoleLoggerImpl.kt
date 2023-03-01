package com.extole.android.sdk.impl

import android.util.Log
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.LogLevel
import com.extole.android.sdk.impl.http.CreativeLoggingEndpoints
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class ExtoleLoggerImpl(private val logLevel: LogLevel = LogLevel.ERROR) : ExtoleLogger {

    override fun debug(message: String, vararg args: Any?) {
        Logger.d(message, args)
    }

    override fun info(message: String, vararg args: Any?) {
        Logger.i(message, args)
    }

    override fun warn(message: String, vararg args: Any?) {
        Logger.w(message, args)
    }

    override fun error(message: String, vararg args: Any?) {
        Logger.e(message, args)
    }

    override fun error(exception: Throwable, message: String, vararg args: Any?) {
        Logger.e(exception, message, args)
    }

    override fun getLogLevel(): LogLevel = logLevel

    companion object {
        private val handler = CoroutineExceptionHandler { _, exception ->
            println("ExtoleLogger got $exception")
        }
    }

    data class Builder(
        var programDomain: String? = null,
        var accessToken: String? = null,
        var context: ExtoleContext = ExtoleContext(),
        var logLevel: LogLevel = LogLevel.ERROR,
        var logSender: ((priority: Int, tag: String?, message: String) -> Unit)? = null
    ) {
        fun withProgramDomain(programDomain: String?) = apply { this.programDomain = programDomain }
        fun withAccessToken(accessToken: String?) = apply { this.accessToken = accessToken }
        fun withContext(context: ExtoleContext) = apply { this.context = context }
        fun withLogLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }
        fun withLogSender(logSender: (priority: Int, tag: String?, message: String) -> Unit) =
            apply { this.logSender = logSender }

        fun build(): ExtoleLogger {
            val programDomainUrl = programDomain
            if (programDomainUrl != null) {
                Logger.clearLogAdapters()
                Logger.addLogAdapter(AndroidLogAdapter())
                Logger.addLogAdapter(
                    ExtoleLogAdapter(
                        programDomainUrl,
                        accessToken,
                        context,
                        logLevel,
                        logSender ?: (this::createLogSender)(programDomainUrl, accessToken)
                    )
                )
            } else {
                Logger.w("ExtoleLogger not registered because programDomain is missing")
            }
            return ExtoleLoggerImpl(logLevel)
        }

        companion object {
            private val sentLogCount = AtomicInteger()
            private const val MAX_LOGS_TO_SEND = 100
        }

        fun createLogSender(
            programDomain: String,
            accessToken: String?
        ): ((priority: Int, tag: String?, message: String) -> Unit) {
            val loggingEndpoints = CreativeLoggingEndpoints(programDomain, accessToken)

            return { priority: Int, tag: String?, message: String ->
                val level = toCreativeLogLevel(priority)
                val logMessage = "$programDomain ${tag.orEmpty()} $message, context: $context"

                if (sentLogCount.incrementAndGet() < MAX_LOGS_TO_SEND) {
                    GlobalScope.launch(handler) {
                        loggingEndpoints.create(level, logMessage)
                    }
                }
            }
        }

        private fun toCreativeLogLevel(priority: Int): String {
            val logLevel = when (priority) {
                Log.ERROR -> "ERROR"
                Log.WARN -> "WARN"
                Log.INFO -> "INFO"
                Log.DEBUG -> "DEBUG"
                else -> "DEBUG"
            }
            return logLevel
        }
    }
}
