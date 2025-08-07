package com.extole.blackbox.sdk

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.extole.blackbox.RetryRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DeeplinkTests {
    @Rule
    @JvmField
    val mRetryTestRule = RetryRule()
    companion object {
        private const val LAUNCH_TIMEOUT = 5000L
        private val WAIT_FOR_ELEMENT_TIMEOUT = TimeUnit.SECONDS.toMillis(30L)
    }

    private lateinit var uiDevice: UiDevice

    @Before
    fun startBrowserAndAccessDeeplinkUrl() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.pressHome()
    }

    @Test
    fun testMobileApplicationIsHandlingDeeplink() {
        simulateLinkClick("https://mobile-monitor.app.link/happy-advocate")

        val deeplinkPageText =
            uiDevice.findObject(UiSelector().resourceId("com.extole:id/deeplink_text"))
        deeplinkPageText.waitForExists(5L * 1000)

        assertThat(deeplinkPageText.exists()).isTrue
    }

    @Test
    fun testOpenExtoleShareLinkWithDeeplinkEnabled() {
        simulateLinkClick("mobile-monitor://simple-deep-link")

        val deepLinkTextArea =
            uiDevice.findObject(UiSelector().resourceId("com.extole:id/deeplink_text"))
        deepLinkTextArea.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        val deepLinkText = uiDevice.findObject(UiSelector().textContains("mobile-monitor"))
        deepLinkText.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)

        assertThat(deepLinkText.text).contains("mobile-monitor")
        assertThat(deepLinkText.text).contains("simple-deep-link")
    }

    @Ignore // Todo Save Failed Tests Screen ENG-21130
    @Test
    fun testDeeplinkActionIsExecutedWithHasDataConditionEvent() {
        simulateLinkClick("mobile-monitor://simple-deep-link?extole_custom_param=custom_value")

        val deepLinkTextArea =
            uiDevice.findObject(UiSelector().resourceId("com.extole:id/deeplink_text"))
        deepLinkTextArea.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        val deepLinkText = uiDevice.findObject(UiSelector().textContains("mobile-monitor"))
        deepLinkText.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)

        val webViewText =
            uiDevice.findObject(UiSelector().textContains("The more you give"))
        webViewText.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        assertThat(webViewText.exists()).isTrue
    }

    private fun simulateLinkClick(link: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse(link)
        context.startActivity(intent)
        uiDevice.wait(
            Until.hasObject(By.pkg("com.extole").depth(0)), LAUNCH_TIMEOUT
        )
    }

    private fun assureChromeBrowserConcentFlow() {
        val chromeBrowserConsent = uiDevice.findObject(UiSelector().text("Accept & continue"))
        chromeBrowserConsent.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        if (chromeBrowserConsent.exists()) {
            chromeBrowserConsent.click()
        }

        val chromeBrowserConsentSyncStep = uiDevice.findObject(UiSelector().text("No thanks"))
        chromeBrowserConsentSyncStep.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        if (chromeBrowserConsentSyncStep.exists()) {
            chromeBrowserConsentSyncStep.click()
        }
    }
}
