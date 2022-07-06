package com.extole.androidsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var sdkActivityContext: Context
    lateinit var extole: Extole
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sdkActivityContext = this

        GlobalScope.launch {
            extole = ServiceLocator.getExtole(this@MainActivity)
            val (zone, campaign) = extole.fetchZone("apply_for_card")
            runOnUiThread {

                findViewById<Button>(R.id.native_share).setText(
                    zone.get("menu.message").toString(), TextView.BufferType.NORMAL
                )

                findViewById<EditText>(R.id.email_message).setText(
                    zone.get("sharing.email.message").toString(), TextView.BufferType.NORMAL
                )

                findViewById<Button>(R.id.email_share).setText(
                    zone.get("sharing.email.subject").toString(), TextView.BufferType.NORMAL
                )

                findViewById<TextView>(R.id.copyright).setText(
                    zone.get("sharing.facebook.description").toString(),
                    TextView.BufferType.NORMAL
                )

                findViewById<Button>(R.id.native_share).setOnClickListener {
                    val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                    startActivity(intent)
                }
            }

            launch {
                val (customMenu1, _) = campaign.fetchZone("custom_menu_1")
                runOnUiThread {
                    findViewById<Button>(R.id.menu_item).text =
                        customMenu1.get("message").toString()
                }
            }
        }
    }
}
