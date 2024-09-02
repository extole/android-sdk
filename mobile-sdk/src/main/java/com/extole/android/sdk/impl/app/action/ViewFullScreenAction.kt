package com.extole.android.sdk.impl.app.action

import android.content.Intent
import com.extole.android.sdk.Action
import com.extole.android.sdk.Action.ActionType.VIEW_FULLSCREEN
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.WebViewFullScreenActivity
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

data class ViewFullScreenAction(@SerializedName("zone_name") val zoneName: String) : Action {

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        extole.getLogger().debug("ActionWebViewFullScreen executor")
        val intent = Intent(
            extole.getContext().getAppContext(),
            WebViewFullScreenActivity::class.java
        )
        extole.getData().forEach {
            intent.putExtra(it.key, it.value)
        }
        event.eventData.forEach {
            intent.putExtra(it.key, it.value as String)
        }
        intent.putExtra("extole_zone_name", zoneName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extole.getContext().getAppContext().startActivity(intent)
    }

    override fun getType(): Action.ActionType = VIEW_FULLSCREEN

    override fun toString(): String {
        return "ActionType: ${getType()}"
    }
}
