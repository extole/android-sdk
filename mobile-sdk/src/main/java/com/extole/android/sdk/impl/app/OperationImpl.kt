package com.extole.android.sdk.impl.app

import com.extole.android.sdk.Action
import com.extole.android.sdk.Condition
import com.extole.android.sdk.Operation
import com.extole.android.sdk.RestException
import com.extole.android.sdk.impl.ExtoleInternal

class OperationImpl(
    private val conditions: List<Condition>?,
    private val actions: List<Action>?
) : Operation {

    override suspend fun executeActions(event: AppEvent, extole: ExtoleInternal) {
        actionsToExecute(event, extole).forEach {
            try {
                if (!extole.getDisabledActions().contains(it.getType())) {
                    extole.getLogger().debug("Executing action ${it.getTitle()}")
                    it.execute(event, extole)
                } else {
                    extole.getLogger().debug("Skipping ${it.getType()} because it is disabled")
                }
            } catch (e: RestException) {
                extole.getLogger().debug("Error executing action ${it.getTitle()}: ${e.message}", e)
            }
        }
    }

    override fun passingConditions(
        event: AppEvent,
        extole: ExtoleInternal
    ): List<Condition> {
        return conditions?.filter { it.passes(event, extole) }.orEmpty()
    }

    override fun actionsToExecute(event: AppEvent, extole: ExtoleInternal): List<Action> {
        if (passingConditions(event, extole).size == conditions?.size) {
            return actions.orEmpty()
        }
        return emptyList()
    }

    override fun getActions(): List<Action> = actions.orEmpty()

    override fun getConditions(): List<Condition> = conditions.orEmpty()

    override fun toString(): String {
        return "Operation Conditions: $conditions, Actions: $actions"
    }
}
