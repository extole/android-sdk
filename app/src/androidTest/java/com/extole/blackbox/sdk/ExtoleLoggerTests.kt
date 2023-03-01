package com.extole.blackbox.sdk

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.LogLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExtoleLoggerTests {
    val logs: MutableMap<LogLevel, MutableList<String>> = mutableMapOf()

    companion object {
        val PROGRAM_DOMAIN = "program-domain"
    }

    @Test
    fun testDefaultLogLevel() {
        val extoleLogger = ExtoleLogger.builder()
            .withProgramDomain(PROGRAM_DOMAIN)
            .withLogSender(this::sendLog)
            .build()
        extoleLogger.debug("debug")
        extoleLogger.info("info")
        extoleLogger.warn("info")
        extoleLogger.error("error")

        assertThat(logs[LogLevel.DEBUG]).isNull()
        assertThat(logs[LogLevel.INFO]).isNull()
        assertThat(logs[LogLevel.WARN]).isNull()
        assertThat(logs[LogLevel.ERROR]).hasSize(1)
            .containsExactly("error")
    }

    @Test
    fun testInfoLogLevel() {
        val extoleLogger = ExtoleLogger.builder()
            .withLogLevel(LogLevel.INFO)
            .withLogSender(this::sendLog)
            .withProgramDomain(PROGRAM_DOMAIN)
            .build()
        extoleLogger.debug("debug")
        extoleLogger.info("info")
        extoleLogger.warn("warn")
        extoleLogger.error("error")
        assertThat(logs[LogLevel.DEBUG]).isNull()
        assertThat(logs[LogLevel.INFO]).hasSize(1)
            .containsExactly("info")
        assertThat(logs[LogLevel.WARN]).hasSize(1)
            .containsExactly("warn")
        assertThat(logs[LogLevel.ERROR]).hasSize(1)
            .containsExactly("error")
    }

    @Test
    fun testWarnLogLevel() {
        val extoleLogger = ExtoleLogger.builder()
            .withLogLevel(LogLevel.WARN)
            .withLogSender(this::sendLog)
            .withProgramDomain(PROGRAM_DOMAIN)
            .build()
        extoleLogger.debug("debug")
        extoleLogger.info("info")
        extoleLogger.warn("warn")
        extoleLogger.error("error")
        assertThat(logs[LogLevel.DEBUG]).isNull()
        assertThat(logs[LogLevel.INFO]).isNull()
        assertThat(logs[LogLevel.WARN]).hasSize(1)
            .containsExactly("warn")
        assertThat(logs[LogLevel.ERROR]).hasSize(1)
            .containsExactly("error")
    }

    @Test
    fun testDebugLogLevel() {
        val extoleLogger = ExtoleLogger.builder()
            .withLogLevel(LogLevel.DEBUG)
            .withLogSender(this::sendLog)
            .withProgramDomain(PROGRAM_DOMAIN)
            .build()
        extoleLogger.debug("debug")
        extoleLogger.info("info")
        extoleLogger.warn("warn")
        extoleLogger.error("error")
        assertThat(logs[LogLevel.DEBUG]).hasSize(1)
            .containsExactly("debug")
        assertThat(logs[LogLevel.INFO]).hasSize(1)
            .containsExactly("info")
        assertThat(logs[LogLevel.WARN]).hasSize(1)
            .containsExactly("warn")
        assertThat(logs[LogLevel.ERROR]).hasSize(1)
            .containsExactly("error")
    }

    @Test
    fun testDisabledLogLevel() {
        val extoleLogger = ExtoleLogger.builder()
            .withProgramDomain(PROGRAM_DOMAIN)
            .withLogSender(this::sendLog)
            .withLogLevel(LogLevel.DISABLE)
            .build()
        extoleLogger.debug("debug")
        extoleLogger.info("info")
        extoleLogger.warn("info")
        extoleLogger.error("error")

        assertThat(logs[LogLevel.DEBUG]).isNull()
        assertThat(logs[LogLevel.INFO]).isNull()
        assertThat(logs[LogLevel.WARN]).isNull()
        assertThat(logs[LogLevel.ERROR]).isNull();
    }

    fun sendLog(
        priority: Int,
        tag: String?,
        message: String
    ) {
        val extoleLogLevel = toExtoleLogLevel(priority)
        if (!logs.containsKey(extoleLogLevel)) {
            logs[extoleLogLevel] = mutableListOf()
        }
        logs[extoleLogLevel]?.add(message)
    }

    private fun toExtoleLogLevel(priority: Int): LogLevel {
        return when (priority) {
            Log.ERROR -> LogLevel.ERROR
            Log.WARN -> LogLevel.WARN
            Log.INFO -> LogLevel.INFO
            Log.DEBUG -> LogLevel.DEBUG
            else -> LogLevel.DEBUG
        }
    }
}
