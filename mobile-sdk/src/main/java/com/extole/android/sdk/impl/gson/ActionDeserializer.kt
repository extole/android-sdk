package com.extole.android.sdk.impl.gson

import com.extole.android.sdk.Action
import com.extole.android.sdk.impl.app.action.FetchAction
import com.extole.android.sdk.impl.app.action.LoadOperationsAction
import com.extole.android.sdk.impl.app.action.NativeShareAction
import com.extole.android.sdk.impl.app.action.PromptAction
import com.extole.android.sdk.impl.app.action.SetLogLevelAction
import com.extole.android.sdk.impl.app.action.UnknownAction
import com.extole.android.sdk.impl.app.action.ViewFullScreenAction
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ActionDeserializer : JsonDeserializer<Action> {
    companion object {
        val typeMap: MutableMap<String, Class<out Action>> = mutableMapOf()

        init {
            typeMap["VIEW_FULLSCREEN"] = ViewFullScreenAction::class.java
            typeMap["FETCH"] = FetchAction::class.java
            typeMap["SET_LOG_LEVEL"] = SetLogLevelAction::class.java
            typeMap["PROMPT"] = PromptAction::class.java
            typeMap["LOAD_OPERATIONS"] = LoadOperationsAction::class.java
            typeMap["NATIVE_SHARE"] = NativeShareAction::class.java
        }
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext
    ): Action {
        val rawType = json.asJsonObject.get("type").asString
        val actionType = typeMap[rawType]
            ?: UnknownAction::class.java
        return context.deserialize(json, actionType)
    }
}
