package com.extole.android.sdk.impl.app.action

import android.util.Log
import com.extole.android.sdk.Action
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent

class UnknownAction : Action {
    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        Log.d("Extole", "UnknownAction executed")
    }

    override fun getType(): Action.ActionType {
        return Action.ActionType.CUSTOM
    }
}
