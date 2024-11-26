package com.extole.blackbox.sdk

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
import com.extole.blackbox.BlackboxNameGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UIInteractionTests {

    companion object {
        private const val EXTOLE_APP_PACKAGE = "com.extole"
        private const val LAUNCH_TIMEOUT = 5000L
        private val WAIT_FOR_ELEMENT_TIMEOUT = TimeUnit.SECONDS.toMillis(30L)
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
            Until.hasObject(By.text("https://mobile-monitor.extole.io")),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun testDataIsCarriedToWebViewOnCtaItemClick() {
        userClicksLogout()
        whenUserClicksOnCtaItem()

        val emailPlaceholder = uiDevice.findObject(
            UiSelector().textMatches(".*extole.io.*")
        )
        emailPlaceholder.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)

        assertThat(emailPlaceholder.text).isNotEmpty()
        assertThat(emailPlaceholder.text).matches(".*extole-monitor-android.extole.io.*")
    }

    @Ignore // Todo Save Failed Tests Screen ENG-21130
    @Test
    fun testSharingIsHandledByProtocolHandlersWhenUsingSms() {
        whenUserOpensShareExperience()

        assureChromeBrowserConcentFlow()

        andEntersHisEmail()

        andClicksOn("sms")

        smsShareDialogIsDisplayed()
    }

    @Ignore // Todo Save Failed Tests Screen ENG-21130
    @Test
    fun testNativeSharingUsingWebView() {
        whenUserOpensShareExperience()

        assureChromeBrowserConcentFlow()

        andEntersHisEmail()

        andClicksOn("native")

        thenNativeShareIsAvailable()
    }

    @Test
    fun testNativeSharingUsingEvents() {
        val customMenuItem =
            uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/native_share"))
        customMenuItem.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        customMenuItem.click()

        assureChromeBrowserConcentFlow()

        val nativeShareView = uiDevice.findObject(
            UiSelector().resourceId("android:id/contentPanel")
                .className("android.widget.ScrollView")
        )
        nativeShareView.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)

        val shareImage = uiDevice.findObject(
            UiSelector().resourceId("android:id/content_preview_filename")
                .className("android.widget.TextView")
        )

        assertThat(shareImage.exists())
        assertThat(shareImage.text).isEqualTo("generic-social.jpg")
    }

    private fun smsShareDialogIsDisplayed() {
        val smsDialogTitle: UiObject = uiDevice.findObject(
            UiSelector().text("New conversation")
        )
        smsDialogTitle.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        assertThat(smsDialogTitle.exists()).isTrue
    }

    private fun andClicksOn(elementText: String) {
        val element: UiObject = uiDevice.findObject(
            UiSelector().text(elementText)
        )
        element.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        assertThat(element.exists()).isTrue
        element.click()
    }

    private fun userClicksLogout() {
        val logoutButton =
            uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/menu_item"))
        logoutButton.click()
    }

    private fun whenUserClicksOnCtaItem() {
        val mainViewButton =
            uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/apply_for_card"))
        mainViewButton.click()

        val webView = uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/webview"))
        webView.waitForExists(TimeUnit.SECONDS.toMillis(3))

        val shareExperience: UiObject = uiDevice.findObject(
            UiSelector().resourceId("extole-share-experience")
        )
        shareExperience.waitForExists(TimeUnit.SECONDS.toMillis(10))
        scroll(3)
    }

    private fun whenUserOpensShareExperience() {
        val mainViewButton =
            uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/main_view"))
        mainViewButton.click()

        val webView = uiDevice.findObject(UiSelector().resourceId("$EXTOLE_APP_PACKAGE:id/webview"))
        webView.waitForExists(TimeUnit.SECONDS.toMillis(3))

        val shareExperience: UiObject = uiDevice.findObject(
            UiSelector().resourceId("extole-share-experience")
        )
        shareExperience.waitForExists(TimeUnit.SECONDS.toMillis(10))
        scroll(3)
    }

    private fun scroll(steps: Int) {
        val scrollStart = (uiDevice.displayHeight * 0.5).toInt()
        val scrollEnd = (uiDevice.displayHeight * 0.4).toInt()
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
        nativeShareView.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)

        val shareMessage = uiDevice.findObject(
            UiSelector().resourceId("android:id/content_preview_text")
                .className("android.widget.TextView")
        )
        assertThat(shareMessage).isNotNull
        assertThat(shareMessage.text)
            .contains("Your Company is great!")
    }

    private fun andEntersHisEmail() {
        scroll(5)
        val emailPlaceholder = uiDevice.findObject(
            UiSelector().resourceIdMatches("extole-tokenizer-input.*")
        )
        val emailInput = emailPlaceholder.getFromParent(
            UiSelector()
                .className("android.widget.EditText")
        )
        emailInput.waitForExists(WAIT_FOR_ELEMENT_TIMEOUT)
        emailInput.click()
        emailInput.clearTextField()
        emailInput.text = BlackboxNameGenerator().getEmailAddress()

        val anotherElement =
            uiDevice.findObject(UiSelector().textStartsWith("Enter"))
        anotherElement.waitForExists(TimeUnit.SECONDS.toMillis(10))
        anotherElement.click()

        val copyLink: UiObject = uiDevice.findObject(
            UiSelector().text("Copy link")
        )
        copyLink.waitForExists(TimeUnit.SECONDS.toMillis(10))
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
