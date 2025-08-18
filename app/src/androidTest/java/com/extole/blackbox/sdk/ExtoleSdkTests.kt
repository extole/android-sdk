package com.extole.blackbox.sdk

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.extole.android.sdk.Extole
import com.extole.blackbox.BlackboxNameGenerator
import com.extole.blackbox.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant


@RunWith(AndroidJUnit4::class)
class ExtoleSdkTests {

    lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testUsingZoneResources() {
        runBlocking {
            val extole =
                Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )

            val (ctaZone, _) = extole.fetchZone("mobile_cta")
            assertThat(ctaZone?.campaignId).isNotNull
            assertThat(ctaZone?.campaignId?.id).isEqualTo("7153581844297128803")
            assertThat(ctaZone?.get("title")).isEqualTo("CTA Item")
        }
    }

    @Test
    fun testRenderInvalidZone() {
        CoroutineScope(SupervisorJob()).launch {
            val extole =
                Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )

            try {
                extole.fetchZone("not_found_zone")
                assertThat(true).isEqualTo(false) //should never be reached
            } catch (exception: RestException) {
                assertThat(exception.message).isEqualTo("No Configured creative.")
            }
        }
    }

    @Test
    fun testIdentifyByCloning() {
        runBlocking {
            val extole =
                Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )

            val sharedPreferences =
                context.getSharedPreferences("extole-data", Context.MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("access_token", null)
            assertThat(accessToken).isNotNull

            extole.clone(identifier = BlackboxNameGenerator().getEmailAddress())
            await().atMost(Duration.ONE_MINUTE)
                .untilAsserted {
                    val secondAccessToken = sharedPreferences.getString("access_token", null)
                    assertThat(secondAccessToken).isNotNull
                    assertThat(accessToken).isNotEqualTo(secondAccessToken)
                }
        }
    }

    @Test
    fun testEarnRewardThenPollForIt() {
        runBlocking {
            val extole =
                Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0")
                )

            val pollingId = RandomStringUtils.randomAlphabetic(8)
            val eventId = extole.sendEvent("mobile_reward", mapOf("polling_id" to pollingId))
            assertThat(eventId).isNotNull

            val reward = extole.getServices().getRewardService().pollReward(pollingId)
            assertThat(reward).isNotNull
            assertThat(reward?.getString("status")).isEqualTo("SUCCEEDED")
            val qualityResults = reward?.getJSONArray("quality_results")
            assertThat(qualityResults?.length()).isEqualTo(1)
            assertThat(qualityResults?.getJSONObject(0)?.getString("score")).isEqualTo("HIGH")
        }
    }

    @Test
    fun testPersonIsEligibleForReward() {
        runBlocking {
            val extole =
                Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0")
                )

            val pollingId = RandomStringUtils.randomAlphabetic(8)
            val eventId = extole.sendEvent(
                "mobile_reward", mapOf(
                    "polling_id" to pollingId,
                    "email" to "person_email@domain.com"
                )
            )
            assertThat(eventId).isNotNull

            val reward = extole.getServices().getRewardService().pollReward(pollingId)
            assertThat(reward).isNotNull
            assertThat(reward?.getString("status")).isEqualTo("FAILED")
            val qualityResults = reward?.getJSONArray("quality_results")
            assertThat(qualityResults?.length()).isEqualTo(1)
            assertThat(qualityResults?.getJSONObject(0)?.getString("score")).isEqualTo("LOW")
        }
    }

    @Test
    fun testLogout() {
        val personEmail = "person-email@mailosaur.com"

        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                    email = personEmail
                )
            }

        val sharedPreferences =
            context.getSharedPreferences("extole-data", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)

        val prefetchedCtaTimestampValue = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }
        val initialTimeStampValue = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val timeValueObtainedFromCache = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        assertThat(prefetchedCtaTimestampValue).isLessThan(initialTimeStampValue);
        assertThat(accessToken).isNotNull
        runBlocking {
            extole.logout()
        }

        val prefetchedCtaTimestampValueAfterLogout = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val accessTokenAfterLogout = sharedPreferences.getString("access_token", null)
        assertThat(accessTokenAfterLogout).isNotEmpty()

        runBlocking {
            extole.identify(BlackboxNameGenerator().getEmailAddress())
            val accessTokenAfterIdentify = sharedPreferences.getString("access_token", null)

            assertThat(accessTokenAfterIdentify).isNotNull()
            assertThat(accessTokenAfterIdentify).isNotEqualTo(accessToken)
        }

        val timestampValueAfterLogout = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val timestampValueAfterLogoutObtainedFromCache = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        assertThat(timestampValueAfterLogout).isEqualTo(timestampValueAfterLogoutObtainedFromCache)
        assertThat(initialTimeStampValue).isEqualTo(timeValueObtainedFromCache)

        val prefetchedCtaTime = Instant.ofEpochSecond(prefetchedCtaTimestampValue)
        val prefetchedCtaTimeAfterLogout =
            Instant.ofEpochSecond(prefetchedCtaTimestampValueAfterLogout)
        val initialTime = Instant.ofEpochSecond(initialTimeStampValue)
        val timeAfterLogout = Instant.ofEpochSecond(timestampValueAfterLogout)

        assertThat(prefetchedCtaTime).isNotEqualTo(prefetchedCtaTimeAfterLogout)
        assertThat(prefetchedCtaTimeAfterLogout).isBefore(timeAfterLogout)
        assertThat(initialTime).isBefore(timeAfterLogout)
    }

    @Test
    fun testIdentifyWillFlushCache() {
        runBlocking(Dispatchers.IO) {
            val personEmail = "person-email@mailosaur.com"
            val extole = Extole.init(
                "mobile-monitor.extole.io",
                context = context, appName = "mobile-monitor", labels = setOf("business"),
                data = mapOf("version" to "1.0"),
                email = personEmail
            )

            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimeStampValue = ctaZone?.get("timestamp") as Long

            extole.identify(BlackboxNameGenerator().getEmailAddress())

            val (ctaZoneTimestamp, _) = extole.fetchZone("mobile_cta_timestamp")
            val timeStampValueAfterIdentify = ctaZoneTimestamp?.get("timestamp") as Long

            val initialTime = Instant.ofEpochSecond(initialTimeStampValue)
            val timeAfterIdentify = Instant.ofEpochSecond(timeStampValueAfterIdentify)
            assertThat(initialTime).isBefore(timeAfterIdentify)
        }
    }

    @Test
    fun testIdentifyJwtWillFlushCache() {
        val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IjUzNmQwNWE2LTMzZWUtNDI2NC04ODI2LW" +
                "JhZDRjOTAyMWZhZiJ9.eyJpc3MiOiJtb2JpbGUtc2RrLmV4dG9sZS5jb20iLCJhdWQiOlsiZXh0b2xlLmNvbSJ" +
                "dLCJlbWFpbCI6InNka3BlcnNvbi1lbWFpbEBtYWlsb3NhdXIuY29tIiwiaWF0IjoxNzA1NTg0Mjg0LCJleHAiO" +
                "jI0ODMxODQyODR9.XdB5-j58GcEeKqKkCLd5f_G78CLLJIHCmsfcOpH-n3o"
        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                )
            }


        val initialTimeStampValue = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val emailBeforeIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }

        assertThat(emailBeforeIdentify).isEqualTo("null")

        runBlocking {
            extole.identifyJwt(jwt)
        }

        val timeStampValueAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val initialTime = Instant.ofEpochSecond(initialTimeStampValue)
        val timeAfterIdentify = Instant.ofEpochSecond(timeStampValueAfterIdentify)
        assertThat(initialTime).isBefore(timeAfterIdentify)


        val emailAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email") as String
        }

        assertThat(emailAfterIdentify).isEqualTo("sdkperson-email@mailosaur.com")
    }

    @Test
    fun testIdentifyJwtWithoutEmail() {
        val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IjUzNmQwNWE2LTMzZWUtNDI2NC04ODI2" +
                "LWJhZDRjOTAyMWZhZiJ9.eyJpc3MiOiJtb2JpbGUtc2RrLmV4dG9sZS5jb20iLCJhdWQiOlsiZXh0b2xlLmN" +
                "vbSJdLCJpYXQiOjE3MDU5MjQwMDksImV4cCI6MjQ4MzUyNDAwOX0.X2GnR6OV9amojSLSzoXeecoujrnMzyY" +
                "As5VWzR86U4M";

        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                )
            }


        val initialTimeStampValue = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val emailBeforeIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }

        assertThat(emailBeforeIdentify).isEqualTo("null")

        runBlocking {
            extole.identifyJwt(jwt)
        }

        val timeStampValueAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val initialTime = Instant.ofEpochSecond(initialTimeStampValue)
        val timeAfterIdentify = Instant.ofEpochSecond(timeStampValueAfterIdentify)
        assertThat(initialTime).isEqualTo(timeAfterIdentify)


        val emailAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }

        assertThat(emailAfterIdentify).isEqualTo("null")
    }

    @Test
    fun testIdentifyWithInvalidJwt() {
        val jwt = "invalid_jwt";

        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                )
            }


        val initialTimeStampValue = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val emailBeforeIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }

        assertThat(emailBeforeIdentify).isEqualTo("null")

        runBlocking {
            extole.identifyJwt(jwt)
        }

        val timeStampValueAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            val initialTimestampValue = ctaZone?.get("timestamp")
            initialTimestampValue as Long
        }

        val initialTime = Instant.ofEpochSecond(initialTimeStampValue)
        val timeAfterIdentify = Instant.ofEpochSecond(timeStampValueAfterIdentify)
        assertThat(initialTime).isEqualTo(timeAfterIdentify)


        val emailAfterIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }

        assertThat(emailAfterIdentify).isEqualTo("null")
    }

    @Test
    fun testIdentifyJwtAtInit() {
        val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IjUzNmQwNWE2LTMzZWUtNDI2NC04ODI2LW" +
                "JhZDRjOTAyMWZhZiJ9.eyJpc3MiOiJtb2JpbGUtc2RrLmV4dG9sZS5jb20iLCJhdWQiOlsiZXh0b2xlLmNvbSJ" +
                "dLCJlbWFpbCI6InNka3BlcnNvbi1lbWFpbEBtYWlsb3NhdXIuY29tIiwiaWF0IjoxNzA1NTg0Mjg0LCJleHAiO" +
                "jI0ODMxODQyODR9.XdB5-j58GcEeKqKkCLd5f_G78CLLJIHCmsfcOpH-n3o"
        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                    jwt = jwt
                )
            }

        val emailBeforeIdentify = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }
        assertThat(emailBeforeIdentify).isEqualTo("sdkperson-email@mailosaur.com")
    }

    @Test
    fun testIdentifyJwtWithoutEmailAndEmailPassedAsASeparateParameter() {
        val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IjUzNmQwNWE2LTMzZWUtNDI2NC04ODI2" +
                "LWJhZDRjOTAyMWZhZiJ9.eyJpc3MiOiJtb2JpbGUtc2RrLmV4dG9sZS5jb20iLCJhdWQiOlsiZXh0b2xlLmN" +
                "vbSJdLCJpYXQiOjE3MDU5MjQwMDksImV4cCI6MjQ4MzUyNDAwOX0.X2GnR6OV9amojSLSzoXeecoujrnMzyY" +
                "As5VWzR86U4M"
        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                    email = "separate-email-person-email@mailosaur.com",
                    jwt = jwt
                )
            }

        val personEmail = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }
        assertThat(personEmail).isEqualTo("separate-email-person-email@mailosaur.com")
    }

    @Test
    fun testIdentifyJwtAtInitWithoutEmailIsInitializedAsAnonymous() {
        val jwt = "invalid_jwt"
        val extole =
            runBlocking {
                return@runBlocking Extole.init(
                    "mobile-monitor.extole.io",
                    context = context, appName = "mobile-monitor", labels = setOf("business"),
                    data = mapOf("version" to "1.0"),
                    jwt = jwt
                )
            }

        val personEmail = runBlocking {
            val (ctaZone, _) = extole.fetchZone("mobile_cta_timestamp")
            ctaZone?.get("email").toString()
        }
        assertThat(personEmail).isEqualTo("null")
    }

    @Test
    fun testFetchZoneWithDifferentClientKeysShouldNotReturnCachedResponse() {
        runBlocking {
            val extole = Extole.init(
                "go.extole.io",
                context = context, 
                appName = "extole-mobile-test", 
                labels = setOf("business"),
                data = mapOf("version" to "1.0"), 
                sandbox = "prod-prod"
            )

            val (zoneGo, campaignGo) = extole.fetchZone("go_configuration", mapOf("clientKey" to "Go"))
            assertThat(zoneGo).isNotNull
            assertThat(campaignGo).isNotNull

            val goDomain = zoneGo?.content?.get("domain")
            val goCampaignId = zoneGo?.campaignId?.id

            val (zoneUc, campaignUc) = extole.fetchZone("go_configuration", mapOf("clientKey" to "Uc"))
            assertThat(zoneUc).isNotNull
            assertThat(campaignUc).isNotNull

            val ucDomain = zoneUc?.content?.get("domain")
            val ucCampaignId = zoneUc?.campaignId?.id

            assertThat(goCampaignId).isEqualTo(ucCampaignId)
            assertThat(goDomain).isNotEqualTo(ucDomain)

            val (zoneGoCached, campaignGoCached) = extole.fetchZone("go_configuration", mapOf("clientKey" to "Go"))
            assertThat(zoneGoCached).isNotNull
            assertThat(campaignGoCached).isNotNull

            assertThat(zoneGoCached?.content?.get("domain")).isEqualTo(goDomain)
            assertThat(zoneGoCached?.content?.get("domain")).isNotEqualTo(ucDomain)
        }
    }

}
