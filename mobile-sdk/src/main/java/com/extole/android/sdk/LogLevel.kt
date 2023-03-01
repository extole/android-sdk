package com.extole.android.sdk

import android.util.Log

enum class LogLevel(val priority: Int) {
    DISABLE(0),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARN(Log.WARN),
    ERROR(Log.ERROR)
}
