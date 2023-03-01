package com.extole.android.sdk.impl

import com.orhanobut.logger.BuildConfig

class ExtoleContext {

    companion object {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val TAGS = listOf("mobile-sdk", "android")
        const val SDK_INTERNAL_VERSION = 1
    }

    override fun toString(): String {
        return "SDK Version: $SDK_INTERNAL_VERSION, $TAGS, appName: $versionName, appVersion: $versionCode"
    }
}
