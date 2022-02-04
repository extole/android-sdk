package com.extole

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.extole.androidsdk.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SdkTest {

    private lateinit var uiDevice: UiDevice

    companion object {
        private const val EXTOLE_APP_PACKAGE = BuildConfig.APPLICATION_ID
        private const val LAUNCH_TIMEOUT = 5000L
        private const val WAIT_ELEMENT_TIMEOUT = 5000L
    }

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
    fun testZoneContentIsCorrectlyLoadedWithExtole() {
        val copyrightElement =
            uiDevice.findObject(UiSelector().textContains("Your Company is great!"))
        copyrightElement.waitForExists(WAIT_ELEMENT_TIMEOUT)
        assertThat(copyrightElement.exists()).isTrue

        val emailTextElement =
            uiDevice.findObject(UiSelector().textContains("I wanted to share"))
        emailTextElement.waitForExists(WAIT_ELEMENT_TIMEOUT)
        assertThat(emailTextElement.exists()).isTrue
    }
}
