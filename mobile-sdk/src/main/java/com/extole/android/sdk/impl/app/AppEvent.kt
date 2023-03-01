package com.extole.android.sdk.impl.app

data class AppEvent(val eventName: String, val eventData: Map<String, Any?> = emptyMap())
