package com.extole.android.sdk

import android.content.Context
import android.webkit.WebView
import com.extole.android.sdk.impl.ApplicationContext
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.app.App
import com.extole.android.sdk.impl.gson.ActionDeserializer
import com.extole.android.sdk.impl.gson.ConditionDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extole is the entry point when integrating by using *Mobile SDK*.
 */

interface Extole {

    /**
     * Used to get zone content, the [zoneName] parameter represents the `zone name`
     * configured in my.extole.com. The accessToken, headers and data associated with this instance
     * of Extole will be included with the request.
     * @return [Pair]<[Zone], [Campaign]> response contains the Zone and Campaign that was selected
     * @throws [RestException]
     */
    @Throws(RestException::class)
    suspend fun fetchZone(
        zoneName: String,
        fethZoneData: Map<String, Any?> = emptyMap()
    ): Pair<Zone?, Campaign>

    /**
     * Returns low-level Extole Services
     * @return [ExtoleServices]
     */
    fun getServices(): ExtoleServices

    /**
     * Logs helps us to monitor the integration
     * @return [ExtoleLogger]
     */
    fun getLogger(): ExtoleLogger

    /**
     * ApplicationContext represents the context where the Mobile SDK is used
     * @return [ApplicationContext]
     */
    fun getContext(): ApplicationContext

    /**
     * Used to send an event, the accessToken, headers and data associated with this instance
     * of Extole will be included with the request
     * @param eventName - the name of the event that will be sent
     * @param data - additional data can be sent with the event
     * @return [Id]<[Event]> the ID of the event that was created
     * @throws [RestException]
     */
    @Throws(RestException::class)
    suspend fun sendEvent(
        eventName: String,
        data: Map<String, Any?> = emptyMap(),
        jwt: String? = null
    ): Id<Event>

    /**
     * Used to send an identifiy event,
     * @param identifier - email of the person to be identified
     * @return [Id]<[Event]> the ID of the event that was created
     * @throws [RestException]
     */
    @Throws(RestException::class)
    suspend fun identify(identifier: String, data: Map<String, String> = mapOf()): Id<Event>

    @Throws(RestException::class)
    suspend fun identifyJwt(jwt: String, data: Map<String, String> = mapOf()): Id<Event>

    /**
     * Used to clear cache and remove current access_token
     */
    fun logout()

    /**
     * Extole cloning is used in cases when customer changed his email address or any other,
     * all parameters are optional and when passed they have priority over the parameters configured
     * on the current instance
     * @param programDomain [String] - your Extole programDomain
     * @param appName - [String], example: Extole-Promo-App
     * @param sandbox - [String], example: production-production, production-test
     * @param context - [ApplicationContext]
     * @param newLabels - [Set]<[String]>
     * @param newData - [Map]<[String], [String]>
     * @param appData - [Map]<[String], [String]>
     * @param email - [String]
     * @return [Extole]
     */
    suspend fun clone(
        programDomain: String? = null,
        appName: String? = null,
        sandbox: String? = null,
        context: ApplicationContext? = null,
        newLabels: Set<String>? = null,
        extendCurrentLabels: Set<String> = emptySet(),
        newData: Map<String, String>? = null,
        extendCurrentData: Map<String, String> = emptyMap(),
        appData: Map<String, String>? = emptyMap(),
        appHeaders: Map<String, String>? = emptyMap(),
        identifier: String? = null,
    ): Extole

    /**
     * Used when integrations are done by using the WebView, the accessToken, headers and
     * data associated with this instance of Extole will be included with the request
     * @param webView - Android WebView component that is used for zones rendering
     * @param headers - optional headers
     * @return data - optional data
     */
    fun webView(
        webView: WebView,
        headers: Map<String, String> = emptyMap(),
        data: Map<String, String> = emptyMap()
    ): ExtoleWebView

    companion object {
        const val ACCESS_TOKEN_PREFERENCES_KEY = "access_token"
        const val PARTNER_SHARE_ID_PREFERENCES_KEY = "partner_share_id"
        const val PROGRAM_DOMAIN_KEY = "com.extole.PROGRAM_DOMAIN"

        const val APP_NAME_KEY = "com.extole.APP_NAME"

        fun registerAction(actionType: String, action: Class<out Action>) {
            ActionDeserializer.typeMap[actionType.uppercase()] = action
        }

        fun registerCondition(conditionType: String, condition: Class<out Condition>) {
            ConditionDeserializer.typeMap[conditionType.uppercase()] = condition
        }

        suspend fun init(
            programDomain: String? = null,
            appName: String? = null,
            sandbox: String = "production-production",
            context: Context,
            labels: Set<String> = emptySet(),
            data: Map<String, String> = emptyMap(),
            appData: Map<String, String> = emptyMap(),
            appHeaders: Map<String, String> = emptyMap(),
            email: String? = null,
            listenToEvents: Boolean = true,
            configurationLoader: ((app: App, data: Map<String, Any>) -> List<Operation>)? = null,
            additionalProtocolHandlers: List<ProtocolHandler> = emptyList(),
            disabledActions: Set<Action.ActionType> = emptySet(),
            jwt: String? = null
        ): Extole {
            return withContext(Dispatchers.IO) {
                return@withContext ExtoleInternal.init(
                    programDomain,
                    appName,
                    sandbox,
                    context,
                    labels,
                    data,
                    appData,
                    appHeaders,
                    email,
                    listenToEvents,
                    configurationLoader,
                    additionalProtocolHandlers,
                    disabledActions,
                    jwt
                )
            }
        }
    }
}
