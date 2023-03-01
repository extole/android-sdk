package com.extole.android.sdk

import com.extole.android.sdk.impl.ExtoleLoggerImpl

/**
 * ExtoleLogger is the interface used to send logs to Extole
 */
interface ExtoleLogger {

    /**
     * Log a debug message
     * @param message - the message that will be logged
     * @param args - optional parameter with values that should be included in the logged message
     */
    fun debug(message: String, vararg args: Any?)

    /**
     * Log an info message
     * @param message - the message that will be logged
     * @param args - optional parameter with values that should be included in the logged message
     */
    fun info(message: String, vararg args: Any?)

    /**
     * Log a warn message
     * @param message - the message that will be logged
     * @param args - optional parameter with values that should be included in the logged message
     */
    fun warn(message: String, vararg args: Any?)

    /**
     * Log an error message
     * @param message - the message that will be logged
     * @param args - optional parameter with values that should be included in the logged message
     */
    fun error(message: String, vararg args: Any?)

    /**
     * Log an error message
     * @param exception - exception that should be included in the log message
     * @param message - the message that will be logged
     * @param args - optional parameter with values that should be included in the logged message
     */
    fun error(exception: Throwable, message: String, vararg args: Any?)

    /**
     * Returns the current log level
     * @return [LogLevel]
     */
    fun getLogLevel(): LogLevel

    companion object {
        fun builder(): ExtoleLoggerImpl.Builder {
            return ExtoleLoggerImpl.Builder()
        }
    }
}
