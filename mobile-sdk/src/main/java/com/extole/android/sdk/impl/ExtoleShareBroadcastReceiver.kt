package com.extole.android.sdk.impl

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.extole.android.sdk.Extole.Companion.ACCESS_TOKEN_PREFERENCES_KEY
import com.extole.android.sdk.Extole.Companion.PARTNER_SHARE_ID_PREFERENCES_KEY
import com.extole.android.sdk.Extole.Companion.PROGRAM_DOMAIN_KEY
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.impl.http.EventEndpoints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ExtoleShareBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val SHARE_EVENT_NAME = "share"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val accessToken =
            context?.let { getSharePreferencesValue(it, ACCESS_TOKEN_PREFERENCES_KEY) }
        val partnerShareId =
            context?.let { getSharePreferencesValue(it, PARTNER_SHARE_ID_PREFERENCES_KEY) }

        val extoleLogger = ExtoleLogger.builder()
            .withProgramDomain(getProgramDomain(context))
            .withAccessToken(accessToken)
            .build()
        val selectedAppPackage =
            (intent?.getExtras()?.get(Intent.EXTRA_CHOSEN_COMPONENT) as ComponentName).packageName
        val programDomain = getProgramDomain(context)
        extoleLogger.debug("User shared by:$selectedAppPackage")
        if (programDomain.isNotEmpty()) {
            GlobalScope.launch {
                if (accessToken.isNullOrEmpty()) {
                    extoleLogger.debug("Access Token is empty")
                }

                if (partnerShareId.isNullOrEmpty()) {
                    extoleLogger.debug("Partner Share Id is empty")
                }

                val eventsApi = EventEndpoints(
                    programDomain,
                    accessToken,
                    emptyMap()
                )
                val requestBody = mutableMapOf<String, Any>()
                requestBody["event_name"] = SHARE_EVENT_NAME

                requestBody["data"] = mapOf(
                    "share.channel" to selectedAppPackage,
                    "partner_share_id" to partnerShareId
                )
                eventsApi.post(requestBody)
            }
        }
    }

    private fun getSharePreferencesValue(context: Context, key: String): String {
        return SharedPreferencesPersistence(context).get(key).orEmpty()
    }

    private fun getProgramDomain(context: Context?): String {
        val extoleLogger = ExtoleLogger.builder().build()
        val applicationInfo = context?.packageManager?.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        val programDomain = applicationInfo?.metaData?.getString(PROGRAM_DOMAIN_KEY)
        if (programDomain.isNullOrEmpty()) {
            extoleLogger.warn("Program Domain is empty")
        }

        return programDomain.toString()
    }
}
