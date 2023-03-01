package com.extole.android.sdk

import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent

interface Condition {

    enum class ConditionType {
        EVENT,
        CUSTOM
    }

    fun passes(event: AppEvent, extole: ExtoleInternal): Boolean
    fun getType(): ConditionType
    fun getTitle(): String = getType().name
}
