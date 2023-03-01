package com.extole.blackbox.sdk

import com.extole.android.sdk.Condition
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

class CustomConditionWithDataParameters(val data: Map<String, String>) :
    Condition {

    companion object {
        val CONDTION_TITLE = "CUSTOM_CONDITION"
    }

    override fun passes(event: AppEvent, extole: ExtoleInternal): Boolean {
        return data.contains(event.eventName)
    }

    override fun getType(): Condition.ConditionType = Condition.ConditionType.CUSTOM
    override fun getTitle(): String = CONDTION_TITLE
}