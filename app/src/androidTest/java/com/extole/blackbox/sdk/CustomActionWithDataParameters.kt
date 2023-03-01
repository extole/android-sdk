package com.extole.blackbox.sdk

import com.extole.android.sdk.Action
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

class CustomActionWithDataParameters(val data: Map<String, String>) : Action {

    companion object {
        val ACTION_TITLE = "CUSTOM_ACTION"
    }

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        extole.getData()["custom_action_key"] = "custom_action_value"
    }

    override fun getType(): Action.ActionType = Action.ActionType.CUSTOM

    override fun getTitle(): String = ACTION_TITLE
}