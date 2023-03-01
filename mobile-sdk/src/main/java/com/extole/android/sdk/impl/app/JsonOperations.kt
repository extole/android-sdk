package com.extole.android.sdk.impl.app

import com.extole.android.sdk.Action
import com.extole.android.sdk.Condition
import com.extole.android.sdk.Operation
import com.extole.android.sdk.impl.gson.ActionDeserializer
import com.extole.android.sdk.impl.gson.ConditionDeserializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class JsonOperations {
    private val operations: List<Operation>

    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer())
            .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
            .create()
        private val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
    }

    constructor(operationsJson: String) {
        this.operations =
            gson.fromJson<List<OperationImpl>?>(operationsJson, operationsType)
    }

    constructor(operationsMap: List<Map<String, Any?>>) {
        val operationsJson = gson.toJson(operationsMap)
        this.operations =
            gson.fromJson<List<OperationImpl>?>(operationsJson, operationsType)
    }

    fun getOperations(): List<Operation> {
        return operations
    }
}
