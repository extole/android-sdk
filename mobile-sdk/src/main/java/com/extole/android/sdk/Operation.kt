package com.extole.android.sdk

import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent

interface Operation {
    suspend fun executeActions(event: AppEvent, extole: ExtoleInternal)
    fun passingConditions(event: AppEvent, extole: ExtoleInternal): List<Condition>
    fun actionsToExecute(event: AppEvent, extole: ExtoleInternal): List<Action>
    fun getActions(): List<Action>
    fun getConditions(): List<Condition>
}
