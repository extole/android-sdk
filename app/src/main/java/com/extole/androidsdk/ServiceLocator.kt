package com.extole.androidsdk

import android.content.Context
import com.extole.android.sdk.Extole
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceLocator private constructor() {
    companion object {

        @Volatile
        private var INSTANCE: Extole? = null
        private val mutex = Mutex()

        suspend fun getExtole(context: Context): Extole =
            INSTANCE ?: mutex.withLock {
                INSTANCE ?: initializeExtole(context).also { INSTANCE = it }
            }

        private fun initializeExtole(context: Context) =
            Extole.init(
                context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
                sandbox = "prod-test", labels = setOf("business"),
                listenToEvents = true
            )
    }
}