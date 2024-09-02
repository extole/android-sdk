package com.extole.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.extole.R
import com.extole.android.sdk.Extole
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SdkActivity : AppCompatActivity() {
    lateinit var extole: Extole
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sdk)

        findViewById<Button>(R.id.main_view).setOnClickListener {
            val intent = Intent(this@SdkActivity, MainActivity::class.java)
            startActivity(intent)
        }

        GlobalScope.launch {
            extole = ServiceLocator.getExtole(this@SdkActivity)
            extole.sendEvent("app_opened")
            val (ctaZone, campaign) = extole.fetchZone("mobile_cta")

            val logoutButton = findViewById<Button>(R.id.menu_item)
            runOnUiThread {
                logoutButton.setOnClickListener {
                    GlobalScope.launch {
                        extole.logout()
                    }
                }
            }

            runOnUiThread {
                findViewById<EditText>(R.id.email_message).setText(
                    ctaZone?.get("title").toString(), TextView.BufferType.NORMAL
                )
            }

            val emailInput = findViewById<TextView>(R.id.email)
            val identifyButton = findViewById<Button>(R.id.identify)
            setIdentifyAction(identifyButton, emailInput)

            runOnUiThread {
                val applyForCardButton = findViewById<Button>(R.id.apply_for_card)
                applyForCardButton.text = ctaZone?.get("title").toString()
                applyForCardButton.setOnClickListener {
                    Log.d("Extole", "tap")
                    GlobalScope.launch {
                        extole.sendEvent("deeplink", mapOf("extole_parameter" to "value",
                            "email" to "android@mailosaur.com"))
                    }
                }
            }
            ctaZone?.viewed()

            findViewById<Button>(R.id.menu_item).setOnClickListener {
                GlobalScope.launch {
                    ctaZone?.tap()
                }
            }
        }
    }

    private fun setIdentifyAction(
        identifyButton: Button,
        emailInput: TextView
    ) {
        identifyButton.setOnClickListener {
            GlobalScope.launch {
                extole = extole.clone(identifier = emailInput.text.toString())
                ServiceLocator.setExtole(extole)
            }
            runOnUiThread {
                Toast.makeText(this@SdkActivity, "Identify event was sent", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}
