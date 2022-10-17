package com.extole.androidsdk

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var extole: Extole
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            val (zone, campaign) = extole.fetchZone("cta_prefetch")
            runOnUiThread {
                findViewById<Button>(R.id.menu_item).setText(
                    zone.get("title").toString(), TextView.BufferType.NORMAL
                )

                findViewById<Button>(R.id.menu_item).setOnClickListener {
                    GlobalScope.launch {
                        extole.sendEvent(zone.get("touch_event").toString())
                    }
                }

                Picasso.get().load(zone.get("image").toString())
                    .into(findViewById<ImageView>(R.id.image_view))
            }

            findViewById<Button>(R.id.deeplink).setOnClickListener {
                GlobalScope.launch {
                    extole.sendEvent("deeplink", mapOf("extole_key" to "extole_value"))
                }
            }
        }
    }
}
