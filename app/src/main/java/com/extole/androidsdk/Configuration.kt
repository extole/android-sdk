package com.extole.androidsdk

import android.content.Context
import com.extole.android.sdk.Extole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Configuration {
    @Singleton
    @Provides
    fun extole(@ApplicationContext context: Context): Extole =
        runBlocking(Dispatchers.IO) {
            return@runBlocking Extole.init(
                context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
                sandbox = "prod-test", labels = setOf("business"),
                listenToEvents = true
            )
        }
}
