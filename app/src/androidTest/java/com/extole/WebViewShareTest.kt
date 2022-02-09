package com.extole.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.extole.androidsdk.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class WebViewShareTest {

    companion object {
        private const val EXTOLE_APP_PACKAGE = BuildConfig.APPLICATION_ID
        private const val LAUNCH_TIMEOUT = 5000L
        private const val WAIT_FOR_ELEMENT_TIMEOUT_SECONDS = 15L
        private const val MAX_EMAIL_LENGTH = 220
        private val MAILBOX_ID: String = "gezt5tev"
        private val EMAIL_DOMAIN = MAILBOX_ID + "@mailosaur.io"
    }

    private lateinit var uiDevice: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.pressHome()

        val launcherPackage: String = uiDevice.launcherPackageName
        assertThat(launcherPackage).isNotNull
        uiDevice.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager
            .getLaunchIntentForPackage(EXTOLE_APP_PACKAGE)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        context.startActivity(intent)

        uiDevice.wait(
            Until.hasObject(By.pkg(EXTOLE_APP_PACKAGE).depth(0)),
            LAUNCH_TIMEOUT
        )

        uiDevice.wait(
            Until.hasObject(By.text("Your Company is great!")),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun testSharingIsHandledByProtocolHandlersWhenUsingSms() {
        whenUserOpensShareExperience()

        andEntersHisEmail()

        andClicksOn("sms")

        smsShareDialogIsDisplayed()
    }

    @Test
    fun testNativeSharingUsingWebView() {
        whenUserOpensShareExperience()

        andEntersHisEmail()

        andClicksOn("native")

        thenNativeShareIsAvailable()
    }

    private fun smsShareDialogIsDisplayed() {
        val smsDialogTitle: UiObject = uiDevice.findObject(
            UiSelector().text("To")
        )
        smsDialogTitle.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))
        assertThat(smsDialogTitle.exists())
            .withFailMessage("Failed to find SMS Dialog title").isTrue
    }

    private fun andClicksOn(elementText: String) {
        val element: UiObject = uiDevice.findObject(
            UiSelector().text(elementText)
        )
        element.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))
        assertThat(element.exists())
            .withFailMessage("Failed to find element: $elementText").isTrue
        element.click()
    }

    private fun whenUserOpensShareExperience() {
        val mainViewButton =
            uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/native_share"))
        mainViewButton.click()

        val webView = uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/webview"))
        webView.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))

        val shareExperience: UiObject = uiDevice.findObject(
            UiSelector().resourceId("extole-share-experience")
        )
        shareExperience.waitForExists(TimeUnit.SECONDS.toMillis(20L))
        scroll(10)
        scroll(10)
    }

    private fun scroll(steps: Int) {
        val scrollStart = (uiDevice.displayHeight * 0.5).toInt()
        val scrollEnd = (uiDevice.displayHeight * 0.2).toInt()
        uiDevice.swipe(
            uiDevice.displayWidth / 2,
            scrollStart,
            uiDevice.displayWidth / 2,
            scrollEnd,
            steps
        )
    }

    private fun thenNativeShareIsAvailable() {
        val nativeShareView = uiDevice.findObject(
            UiSelector().resourceId("android:id/contentPanel")
                .className("android.widget.ScrollView")
        )
        nativeShareView.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))

        val shareMessage = uiDevice.findObject(
            UiSelector().resourceId("android:id/content_preview_text")
                .className("android.widget.TextView")
        )
        assertThat(shareMessage.exists())
        assertThat(shareMessage.text)
            .contains("Sign up today and we'll both get \$10!")
    }

    private fun andEntersHisEmail() {
        val emailPlaceholder = uiDevice.findObject(
            UiSelector().textContains("Your email")
        )
        emailPlaceholder.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))
        val emailInput = emailPlaceholder.getFromParent(
            UiSelector()
                .className("android.widget.EditText")
        )
        emailInput.waitForExists(TimeUnit.SECONDS.toMillis(WAIT_FOR_ELEMENT_TIMEOUT_SECONDS))
        emailInput.click()
        emailInput.clearTextField()
        emailInput.text = getEmailAddress()

        val enterButton =
            uiDevice.findObject(UiSelector().text("Enter").className("android.widget.Button"))
        enterButton.waitForExists(TimeUnit.SECONDS.toMillis(20))
        enterButton.click()

        val copyLink: UiObject = uiDevice.findObject(
            UiSelector().text("Copy link")
        )
        copyLink.waitForExists(TimeUnit.SECONDS.toMillis(20))
    }

    fun getEmailAddress(prefix: String = "p"): String {
        val generatedEmail = prefix + "-" + System.nanoTime().toString() + "." + EMAIL_DOMAIN
        if (generatedEmail.length > MAX_EMAIL_LENGTH) {
            throw Exception("Email prefix is too long:$prefix")
        }
        return generatedEmail
    }
}
