package com.extole.android.sdk.impl.app.condition

import com.extole.android.sdk.Condition
import com.extole.android.sdk.Condition.ConditionType.EVENT
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

class EventCondition(
    @SerializedName("event_names") val eventNames: List<String>,
    @SerializedName("has_data_keys") val hasDataKeys: Set<String>? = emptySet(),
    @SerializedName("has_data_values") val hasDataValues: Set<String>? = emptySet()
) : Condition {

    override fun passes(event: AppEvent, extole: ExtoleInternal): Boolean {
        val eventNameMatches = eventNames.any { it.lowercase() == event.eventName.lowercase() }
        val keyMatches = hasDataKeys == null || hasDataKeys.isEmpty() || hasDataKeys.any { key ->
            event.eventData.keys.any { eventDataKey -> eventDataKey.matches(Regex(key)) }
        }
        val valuesMatches =
            hasDataValues == null || hasDataValues.isEmpty() || hasDataValues.any { value ->
                event.eventData.values.any { eventDataValue ->
                    eventDataValue.toString().matches(Regex(value))
                }
            }
        val passes = eventNameMatches && keyMatches && valuesMatches
        extole.getLogger().debug(
            "EventCondition passes=$passes, " +
                    "eventData=${event.eventData}, keys=$hasDataKeys, values=$hasDataValues"
        )
        return passes
    }

    override fun getType(): Condition.ConditionType = EVENT

    override fun toString(): String {
        return "ConditionType: ${getType()}, eventNames: $eventNames"
    }
}
