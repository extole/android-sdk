package com.extole.android.sdk.impl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.extole.android.sdk.Persistence

class ApplicationContext(
    private val context: Context,
    private val persistence: Persistence<String, String>?
) {

    fun getApplicationInfo() = context.packageManager?.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    )

    fun getPersistence(): Persistence<String, String> {
        return persistence ?: SharedPreferencesPersistence(context)
    }

    fun getAppContext() = context

    fun startActivity(intent: Intent) {
        context.startActivity(intent)
    }
}
