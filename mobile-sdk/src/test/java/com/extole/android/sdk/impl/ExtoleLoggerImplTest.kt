package com.extole.android.sdk.impl

import com.orhanobut.logger.LogAdapter
import com.orhanobut.logger.Logger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExtoleLoggerImplTest {
    private val loggedMessages = mutableListOf<String>()
    private val logger = ExtoleLoggerImpl()

    @Before
    fun captureLogs() {
        Logger.clearLogAdapters()
        Logger.addLogAdapter(object : LogAdapter {
            override fun isLoggable(priority: Int, tag: String?): Boolean = true

            override fun log(priority: Int, tag: String?, message: String) {
                loggedMessages.add(message)
            }
        })
    }

    @After
    fun releaseLogs() {
        Logger.clearLogAdapters()
    }

    @Test
    fun testMessageWithPercentEncodedUrlIsLoggedVerbatim() {
        val message = "Http error request (url=https://share.example.com/zone/x?time=10%3A00&ids=1%2C2)"

        logger.debug(message)
        logger.info(message)
        logger.warn(message)
        logger.error(message)
        logger.error(IllegalStateException("failure"), message)

        assertEquals(5, loggedMessages.size)
        loggedMessages.forEach { assertTrue(it, it.contains(message)) }
    }

    @Test
    fun testArgsAreFormattedIntoMessage() {
        logger.info("Executing action %s: %s", "VIEW_FULLSCREEN", "10%3A00")

        val loggedMessage = loggedMessages.single()
        assertTrue(loggedMessage, loggedMessage.contains("Executing action VIEW_FULLSCREEN: 10%3A00"))
    }
}
