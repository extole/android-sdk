package com.extole.android.sdk.impl.app.action

import android.content.Intent
import android.net.Uri
import com.extole.android.sdk.Action
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.AppEvent
import com.google.gson.annotations.SerializedName

data class NativeShareAction(
    @SerializedName("zone") val zone: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("image") val image: String?
) : Action {
    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        val imageUri: Uri = Uri.parse(image)
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareMessage(extole))
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        shareIntent.type = "image/jpeg"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooser = Intent.createChooser(shareIntent, "send")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extole.getContext().startActivity(chooser)
    }

    private suspend fun getShareMessage(extole: ExtoleInternal): String {
        if (message == null && zone != null) {
            val zoneResponse = extole.getServices().getZoneService().getZones(
                setOf(zone),
                extole.getData(),
                extole.getLabels()
            )

            return zoneResponse.get(zone, extole.getData())?.get("message").toString()
        }
        return message ?: ""
    }

    override fun getType(): Action.ActionType = Action.ActionType.NATIVE_SHARE
}
