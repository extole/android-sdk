package com.extole.blackbox.sdk

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.extole.android.sdk.Action
import com.extole.android.sdk.Action.ActionType.FETCH
import com.extole.android.sdk.Action.ActionType.LOAD_OPERATIONS
import com.extole.android.sdk.Action.ActionType.NATIVE_SHARE
import com.extole.android.sdk.Action.ActionType.PROMPT
import com.extole.android.sdk.Action.ActionType.SET_LOG_LEVEL
import com.extole.android.sdk.Action.ActionType.VIEW_FULLSCREEN
import com.extole.android.sdk.Condition
import com.extole.android.sdk.Condition.ConditionType.CUSTOM
import com.extole.android.sdk.Condition.ConditionType.EVENT
import com.extole.android.sdk.Extole
import com.extole.android.sdk.LogLevel
import com.extole.android.sdk.Operation
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.ZoneResponseKey
import com.extole.android.sdk.impl.app.App
import com.extole.android.sdk.impl.app.AppEvent
import com.extole.android.sdk.impl.app.OperationImpl
import com.extole.android.sdk.impl.app.action.FetchAction
import com.extole.android.sdk.impl.app.action.PromptAction
import com.extole.android.sdk.impl.app.action.SetLogLevelAction
import com.extole.android.sdk.impl.app.action.ViewFullScreenAction
import com.extole.android.sdk.impl.app.condition.EventCondition
import com.extole.android.sdk.impl.gson.ActionDeserializer
import com.extole.android.sdk.impl.gson.ConditionDeserializer
import com.extole.blackbox.sdk.CustomAction.Companion.ACTION_TITLE
import com.extole.blackbox.sdk.CustomCondition.Companion.CONDTION_TITLE
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppEngineTests {

    lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testFlowConditionsAreEvaluatedActionsAreSelectedForExecution() {
        val appOpenCondition = EventCondition(listOf("onAppOpen"))
        val actionDeeplink = PromptAction("message")
        val onAppOpenDeepLinkAction =
            OperationImpl(listOf(appOpenCondition), listOf(actionDeeplink))

        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )
            val actionsToExecute =
                onAppOpenDeepLinkAction.actionsToExecute(
                    AppEvent("onAppOpen"),
                    extole
                )
            val passingConditions =
                onAppOpenDeepLinkAction.passingConditions(
                    AppEvent("onAppOpen"),
                    extole
                )
            assertThat(actionsToExecute).hasSize(1)
                .extracting("type").containsExactly(PROMPT)
            assertThat(passingConditions).hasSize(1)
                .extracting("type").containsExactly(EVENT)
        }
    }

    @Test
    fun testNoActionIsSelectedForExecutionWhenNotAllConditionsArePassing() {
        val appOpenCondition = EventCondition(listOf("onAppOpen"))
        val appClosedCondition = EventCondition(listOf("onAppClosed"))
        val actionDeeplink = PromptAction("message")
        val onAppOpenDeepLinkAction =
            OperationImpl(listOf(appOpenCondition, appClosedCondition), listOf(actionDeeplink))

        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )
            val actionsToExecute = onAppOpenDeepLinkAction.actionsToExecute(
                AppEvent("onAppOpen"),
                extole
            )
            val passingConditions = onAppOpenDeepLinkAction.passingConditions(
                AppEvent("onAppOpen"),
                extole
            )
            assertThat(actionsToExecute).hasSize(0)
            assertThat(passingConditions).hasSize(1)
                .extracting("type").containsExactly(EVENT)
        }
    }

    @Test
    fun testActionAndConditionDeserializers() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer())
            .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
            .create()

        val operationsJson = """
            [
              {
                "conditions": [
                  {
                    "type": "EVENT",
                    "event_names": [
                      "onAppOpen"
                    ]
                  }
                ],
                "actions": [
                  {
                    "type": "VIEW_FULLSCREEN",
                    "zone_name": "welcome_offer"
                  },
                  {
                    "type": "FETCH",
                    "zones": [
                      "mobile_menu",
                      ""
                    ]
                  },
                  {
                    "type": "SET_LOG_LEVEL",
                    "log_level": "ERROR"
                  }
                ]
              }
            ]
        """
        val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
        val operations: List<OperationImpl> = gson.fromJson(operationsJson, operationsType)

        assertThat(operations).hasSize(1)
        assertThat(operations[0].getConditions()).hasSize(1)
        assertThat(operations[0].getConditions()[0].getType()).isEqualTo(EVENT)
        assertThat((operations[0].getConditions()[0] as EventCondition).eventNames).containsExactly(
            "onAppOpen"
        )

        assertThat(operations[0].getActions()).hasSize(3)
        assertThat(operations[0].getActions()).extracting("type")
            .containsExactlyInAnyOrder(VIEW_FULLSCREEN, FETCH, SET_LOG_LEVEL)
        assertThat((operations[0].getActions()[0] as ViewFullScreenAction).zoneName).isEqualTo("welcome_offer")
        assertThat((operations[0].getActions()[1] as FetchAction).zones).containsExactly(
            "mobile_menu",
            ""
        )
        assertThat((operations[0].getActions()[2] as SetLogLevelAction).logLevel).isEqualTo("ERROR")
    }

    @Test
    fun testUnknownDataIsDeserializedAsAMap() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer())
            .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
            .create()

        val operationsJson = """
            [
              {
                "conditions": [
                  {
                    "type": "CUSTOM_CONDITION",
                     "data": {
                        "event_name": "name"
                    }
                  }
                ],
                "actions": [
                  {
                    "type": "CUSTOM_ACTION",
                    "data": {
                        "custom_key": "custom_value"
                    }
                  }
                ]
              }
            ]
        """
        Extole.registerAction(ACTION_TITLE, CustomActionWithDataParameters::class.java)
        Extole.registerCondition(CONDTION_TITLE, CustomConditionWithDataParameters::class.java)
        val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
        val operations = gson.fromJson<List<OperationImpl>>(operationsJson, operationsType)

        assertThat(operations).hasSize(1)
        assertThat(operations[0].getConditions()).hasSize(1)
        assertThat(operations[0].getConditions()[0].getType()).isEqualTo(CUSTOM)
        assertThat(operations[0].getConditions()[0].getTitle()).isEqualTo(CONDTION_TITLE)
        assertThat((operations[0].getConditions()[0] as CustomConditionWithDataParameters).data)
            .containsKey("event_name")
            .containsValue("name")

        assertThat(operations[0].getActions()).hasSize(1)
        assertThat(operations[0].getActions()).extracting("type")
            .containsExactlyInAnyOrder(Action.ActionType.CUSTOM)
        assertThat(operations[0].getActions()).extracting("title")
            .containsExactlyInAnyOrder(ACTION_TITLE)
        assertThat((operations[0].getActions()[0] as CustomActionWithDataParameters).data)
            .containsKey("custom_key")
            .containsValue("custom_value")
    }

    @Test
    fun testCustomActionAndConditionsAreDeserialized() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer())
            .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
            .create()

        val operationsJson = """
            [
              {
                "conditions": [
                  {
                    "type": "CUSTOM_CONDITION",
                    "custom_parameter": [
                      "custom_value"
                    ]
                  }
                ],
                "actions": [
                  {
                    "type": "CUSTOM_ACTION",
                    "custom_parameter": "custom_value"
                  }
                ]
              }
            ]
        """

        Extole.registerAction(ACTION_TITLE, CustomAction::class.java)
        Extole.registerCondition(CONDTION_TITLE, CustomCondition::class.java)

        val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
        val operations = gson.fromJson<List<OperationImpl>>(operationsJson, operationsType)

        assertThat(operations).hasSize(1)
        assertThat(operations[0].getConditions()).hasSize(1)
        assertThat(operations[0].getConditions()[0].getType()).isEqualTo(CUSTOM)
        assertThat(operations[0].getConditions()[0].getTitle()).isEqualTo(CONDTION_TITLE)
        assertThat((operations[0].getConditions()[0] as CustomCondition).customParameter)
            .containsExactly("custom_value")

        assertThat(operations[0].getActions()).hasSize(1)
        assertThat(operations[0].getActions()).extracting("type")
            .containsExactlyInAnyOrder(Action.ActionType.CUSTOM)
        assertThat(operations[0].getActions()).extracting("title")
            .containsExactlyInAnyOrder(ACTION_TITLE)
        assertThat((operations[0].getActions()[0] as CustomAction).customParameter)
            .isEqualTo("custom_value")
    }

    @Test
    fun testCustomActionsAndConditionsAreSelectedForExecution() {
        Extole.registerAction(ACTION_TITLE, CustomAction::class.java)
        Extole.registerCondition(CONDTION_TITLE, CustomCondition::class.java)

        val programDomain = "mobile-monitor.extole.io"
        val extole =
            ExtoleInternal.init(
                programDomain,
                context = context, appName = "extole-mobile-test", labels = setOf("business"),
                data = mapOf("version" to "1.0"), sandbox = "prod-test",
                configurationLoader = flowWithCustomConditionsAndActionsLoader()
            )

        await().atMost(Duration.TEN_SECONDS).untilAsserted {
            assertThat(extole.getData().keys).contains("custom_action_key")
            assertThat(extole.getData().values).contains("custom_action_value")
        }
    }

    @Test
    fun testConfiguredFlowIsExecuted() {
        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )
            assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            assertThat(extole.getZonesResponse().getAll()).hasSize(0)

            await().atMost(Duration.TEN_SECONDS).untilAsserted {
                assertThat(extole.getOperations()).hasSize(4)
                assertThat(extole.getOperations().map { it as OperationImpl }
                    .flatMap { it.getConditions() }
                    .groupBy { it.getType() }.keys).containsOnly(EVENT)
                assertThat(extole.getOperations().map { it as OperationImpl }
                    .flatMap { it.getActions() }
                    .map { it.getType() }).containsExactlyInAnyOrder(
                    LOAD_OPERATIONS,
                    FETCH,
                    NATIVE_SHARE,
                    VIEW_FULLSCREEN
                )
                assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            }
        }
    }

    @Test
    fun testOperationsWithoutConditionsAreNotExecuted() {
        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test",
                    configurationLoader = flowWithoutConditionsLoader()
                )
            assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            assertThat(extole.getZonesResponse().getAll()).hasSize(0)

            await().atMost(Duration.TEN_SECONDS).untilAsserted {
                assertThat(extole.getZonesResponse().getAll()).hasSize(0)
                assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            }
        }
    }

    @Test
    fun testMobileMonitorOperationsAreExecuted() {
        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )
            await().atMost(Duration.TEN_SECONDS).untilAsserted {
                assertThat(extole.getZonesResponse().getAll()).hasSize(1)
                assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            }
        }
    }

    @Test
    fun testZonesArePrefetched() {
        runBlocking {
            val programDomain = "mobile-monitor.extole.io"
            val extole =
                ExtoleInternal.init(
                    programDomain,
                    context = context, appName = "extole-mobile-test", labels = setOf("business"),
                    data = mapOf("version" to "1.0"), sandbox = "prod-test"
                )
            assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            assertThat(extole.getZonesResponse().getAll()).hasSize(0)

            await().atMost(Duration.TEN_SECONDS).untilAsserted {
                assertThat(extole.getZonesResponse().getAll()).hasSize(1)
                assertThat(extole.getZonesResponse().getAll().keys)
                    .extracting("zoneName")
                    .containsExactlyInAnyOrder(
                        "mobile_cta"
                    )
                assertThat(extole.getLogger().getLogLevel()).isEqualTo(LogLevel.ERROR)
            }
        }
    }

    private fun flowWithoutConditionsLoader(): (app: App, data: Map<String, Any>) -> List<Operation> {
        return { _, _ ->
            val operationsJson = """
            [
              {
                "actions": [
                  {
                    "type": "FETCH",
                    "zones": [
                      "mobile_promotion",
                      "apply_for_card"
                    ]
                  },
                  {
                    "type": "SET_LOG_LEVEL",
                    "log_level": "DEBUG"
                  }
                ]
              }
            ]
        """

            val gson = GsonBuilder()
                .registerTypeAdapter(Action::class.java, ActionDeserializer())
                .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
                .create()
            val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
            gson.fromJson<List<OperationImpl>>(operationsJson, operationsType)
        }
    }

    private fun flowWithCustomConditionsAndActionsLoader(): (app: App, data: Map<String, Any>) -> List<Operation> {
        return { _, _ ->
            val operationsJson = """
            [
              {
                "conditions": [
                  {
                    "type": "CUSTOM_CONDITION",
                    "custom_parameter": [
                      "custom_value",
                      "app_initialized"
                    ]
                  }
                ],
                "actions": [
                  {
                    "type": "CUSTOM_ACTION",
                    "custom_parameter": "custom_value"
                  }
                ]
              }
            ]
        """

            val gson = GsonBuilder()
                .registerTypeAdapter(Action::class.java, ActionDeserializer())
                .registerTypeAdapter(Condition::class.java, ConditionDeserializer())
                .create()
            val operationsType = object : TypeToken<List<OperationImpl>>() {}.type
            gson.fromJson<List<OperationImpl>>(operationsJson, operationsType)
        }
    }

}
