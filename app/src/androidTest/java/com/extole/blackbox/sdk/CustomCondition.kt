package com.extole.blackbox.sdk

import com.extole.android.sdk.Condition
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

class CustomCondition(@SerializedName("custom_parameter") val customParameter: Array<String>) :
    Condition {

    companion object {
        val CONDTION_TITLE = "CUSTOM_CONDITION"
    }

    override fun passes(event: AppEvent, extole: ExtoleInternal): Boolean {
        return customParameter.contains(event.eventName)
    }

    override fun getType(): Condition.ConditionType = Condition.ConditionType.CUSTOM
    override fun getTitle(): String = CONDTION_TITLE
}