package com.extole.androidsdk

import android.content.Context
import com.extole.android.sdk.Extole

object ServiceLocator {
    @JvmStatic
    @Volatile
    private var extoleSdk: Extole? = null

    suspend fun getExtole(context: Context): Extole {
        if (extoleSdk != null) {
            return extoleSdk!!
        }

        // singleton init to be fixed
        extoleSdk = Extole.init(
            context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
            sandbox = "prod-test", debugEnabled = true, labels = setOf("business")
        )

        return extoleSdk!!
    }

    fun setExtole(extole: Extole) {
        this.extoleSdk = extole
    }
}
