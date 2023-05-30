package com.extole.androidsdk

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.FutureTask
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var extoleFuture: FutureTask<Extole>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            // example 1
            val extole = initExtole(this@MainActivity)

            // example 2
            val extole2 = withContext(Dispatchers.IO) {
                extoleFuture.get()
            }

            val (zone, campaign) = extole.fetchZone("mobile_cta")
            runOnUiThread {
                findViewById<Button>(R.id.menu_item).setText(
                    zone?.get("title").toString(), TextView.BufferType.NORMAL
                )

                findViewById<Button>(R.id.menu_item).setOnClickListener {
                    GlobalScope.launch {
                        zone?.tap()
                    }
                }
            }

            findViewById<Button>(R.id.deeplink).setOnClickListener {
                GlobalScope.launch {
                    extole.sendEvent("deeplink", mapOf("extole_key" to "extole_value"))
                }
            }
        }
    }

    suspend fun initExtole(context: Context): Extole {
        return withContext(Dispatchers.IO) {
            return@withContext Extole.init(
                context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
                sandbox = "prod-test", labels = setOf("business"),
                listenToEvents = true
            )
        }
    }
}
