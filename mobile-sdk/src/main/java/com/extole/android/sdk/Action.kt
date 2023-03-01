package com.extole.android.sdk

import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent

interface Action {

    enum class ActionType {
        VIEW_FULLSCREEN,
        PROMPT,
        SET_LOG_LEVEL,
        FETCH,
        LOAD_OPERATIONS,
        NATIVE_SHARE,
        CUSTOM
    }

    suspend fun execute(event: AppEvent, extole: ExtoleInternal)

    fun getType(): ActionType
    fun getTitle(): String = getType().name
}
