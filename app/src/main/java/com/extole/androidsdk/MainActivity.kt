package com.extole.androidsdk

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.extole.android.sdk.Extole
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            val extole = initExtoleSdk(this@MainActivity)
            extole.event("mobile_view", mapOf(Pair("partner_user_id", "123")))
            loadCreative(extole)
            configureNativeSharingButton(extole)

            runOnUiThread {
                val shareImage: ImageView = findViewById(R.id.share_image)
                Picasso.get().load(Uri.parse(extole.getText("sharing.facebook.image"))).into(shareImage);
            }
        }
    }

    private fun configureNativeSharingButton(
        extole: Extole
    ) {
        val nativeShare: Button = findViewById(R.id.native_share)
        nativeShare.setOnClickListener {
            extole.nativeShare(
                extole.getText("sharing.email.subject"),
                extole.getText("sharing.email.message") + " " + extole.getText("me.shareable_link")
            )
        }
    }

    fun loadCreative(extole: Extole) {
        val menuItem: Button = findViewById(R.id.menu_item)
        val copyright: TextView = findViewById(R.id.copyright)
        menuItem.text = extole.getText("calls_to_action.menu.message")
        copyright.text = extole.getText("links.company_url")

        val emailMessage: EditText = findViewById(R.id.email_message)
        emailMessage.setText(extole.getText("sharing.email.message"), TextView.BufferType.EDITABLE)
        val emailAddress: EditText = findViewById(R.id.email_address)

        val emailShare: Button = findViewById(R.id.email_share)
        emailShare.setOnClickListener {
            GlobalScope.launch {
                extole.emailShare(emailAddress.text.toString(), emailMessage.text.toString())
            }
        }
    }

    fun initExtoleSdk(context: ComponentActivity): Extole {
        return Extole.Builder("https://extole-monitor-android.extole.io/", context)
            .withAppName("extole-mobile-test")
            .addAppData("version", "1.0")
            .withSandbox("prod-test")
            .withDebugEnabled(true)
            .build()
    }
}