package com.extole.android.sdk.impl.app

import com.extole.android.sdk.Operation
import com.extole.android.sdk.impl.ExtoleInternal

class AppEngine(
    private val operations: List<Operation>,
) {

    suspend fun execute(appEvent: AppEvent, extole: ExtoleInternal) {
        operations.forEach {
            extole.getLogger().debug("Executing operation: $it")
            it.executeActions(appEvent, extole)
        }
    }
}
