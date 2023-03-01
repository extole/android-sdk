package com.extole.android.sdk.impl.gson

import com.extole.android.sdk.Condition
import com.extole.android.sdk.impl.app.condition.EventCondition
import com.extole.android.sdk.impl.app.condition.UnknownCondition
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ConditionDeserializer : JsonDeserializer<Condition> {
    companion object {
        val typeMap: MutableMap<String, Class<out Condition>> = mutableMapOf()

        init {
            typeMap["EVENT"] = EventCondition::class.java
        }
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext
    ): Condition {
        val rawType = json.asJsonObject.get("type").asString
        val conditionType = typeMap[rawType]
            ?: UnknownCondition::class.java
        return context.deserialize(json, conditionType)
    }
}
