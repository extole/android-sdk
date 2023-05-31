package com.extole.mobile

import android.content.Context
import com.extole.android.sdk.Extole
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceLocator private constructor() {
    companion object {

        @Volatile
        private lateinit var instance: Extole
        private val mutex = Mutex()

        suspend fun getExtole(context: Context): Extole {
            if (!this::instance.isInitialized) {
                mutex.withLock {
                    if (!this::instance.isInitialized) {
                        initializeExtole(context).also {
                            instance = it
                        }
                    }
                }
            }
            return instance
        }

        suspend fun setExtole(extole: Extole) = mutex.withLock {
            instance = extole
        }

        private suspend fun initializeExtole(context: Context) =
            Extole.init(
                context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
                sandbox = "prod-test", labels = setOf("business"),
                listenToEvents = true
            )
    }
}
