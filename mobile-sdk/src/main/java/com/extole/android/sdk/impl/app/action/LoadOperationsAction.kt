package com.extole.android.sdk.impl.app.action

import android.os.Build
import android.provider.Settings
import com.extole.android.sdk.Action
import com.extole.android.sdk.Action.ActionType.LOAD_OPERATIONS
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.App.LOAD_DONE_EVENT
import com.extole.android.sdk.impl.app.AppEngine
import com.extole.android.sdk.impl.app.AppEvent
import com.extole.android.sdk.impl.app.JsonOperations
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray

data class LoadOperationsAction(
    @SerializedName("zones") val zones: List<String>,
    @SerializedName("data") val data: Map<String, String>? = emptyMap(),
) : Action {
    companion object {
        var loadOperationActions = HashSet<LoadOperationsAction>()
    }

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        withContext(Dispatchers.IO) {
            removeExecutedActionsFromQueue()

            val allData = prepareRequestData(extole)
            val loadedContent = extole.getServices().getZoneService().getZones(
                zones.toSet(),
                allData,
                extole.getLabels()
            )
            loadedContent.getAll().forEach { entry ->
                entry.value?.let {
                    val operationsZonesContent = it.get("operations")
                    if (operationsZonesContent != null) {
                        val operations =
                            JsonOperations(operationsZonesContent as List<Map<String, Any?>>)
                                .getOperations()
                        extole.getOperations().addAll(operations)
                        val jsonOperations = JSONArray(operationsZonesContent)
                        for (element in 0 until jsonOperations.length()) {
                            extole.getJsonConfiguration()
                                .add(jsonOperations.getJSONObject(element))
                        }
                        val additionalLoadOperations = operations.flatMap { it.getActions() }
                            .filter { it.getType() == LOAD_OPERATIONS }
                            .map { it as LoadOperationsAction }
                        loadOperationActions.addAll(additionalLoadOperations)
                        AppEngine(operations).execute(AppEvent("on_load"), extole)
                    }
                }
            }

            if (loadOperationActions.isEmpty()) {
                EventBus.getDefault().post(AppEvent(LOAD_DONE_EVENT))
            }
        }
    }

    private fun prepareRequestData(extole: ExtoleInternal): MutableMap<String, String> {
        val deviceId = Settings.Secure.getString(
            extole.getContext().getAppContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val allData = extole.getData().toMutableMap()
        allData.putAll(data ?: emptyMap())
        allData["device_id"] = deviceId
        allData["os"] = Build.VERSION.RELEASE
        return allData
    }

    private fun removeExecutedActionsFromQueue() {
        val executedOperations = loadOperationActions
            .filter { it.zones.containsAll(zones) }
        executedOperations.forEach {
            loadOperationActions.remove(it)
        }
    }

    override fun getType(): Action.ActionType = LOAD_OPERATIONS

    override fun toString(): String {
        return "Action: ${getType()}, zones: $zones"
    }
}
