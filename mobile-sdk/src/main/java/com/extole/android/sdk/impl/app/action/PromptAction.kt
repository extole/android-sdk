package com.extole.android.sdk.impl.app.action

import android.widget.Toast
import com.extole.android.sdk.Action
import com.extole.android.sdk.Action.ActionType.PROMPT
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

data class PromptAction(@SerializedName("message") val message: String) : Action {

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        extole.getLogger().debug("ActionPrompt executor, message=$message")
        Toast.makeText(
            extole.getContext().getAppContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun getType(): Action.ActionType = PROMPT

    override fun toString(): String {
        return "Action: ${getType()}"
    }
}
