package com.extole.android.sdk.impl.app.condition

import com.extole.android.sdk.Condition
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent

class UnknownCondition : Condition {
    override fun passes(event: AppEvent, extole: ExtoleInternal): Boolean {
        return false
    }

    override fun getType(): Condition.ConditionType {
        return Condition.ConditionType.CUSTOM
    }
}
