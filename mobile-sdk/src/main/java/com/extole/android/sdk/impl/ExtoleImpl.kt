package com.extole.android.sdk.impl

import android.content.pm.PackageManager
import android.webkit.WebView
import com.extole.android.sdk.Action
import com.extole.android.sdk.Campaign
import com.extole.android.sdk.Event
import com.extole.android.sdk.Extole
import com.extole.android.sdk.Extole.Companion.ACCESS_TOKEN_PREFERENCES_KEY
import com.extole.android.sdk.Extole.Companion.PARTNER_SHARE_ID_PREFERENCES_KEY
import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.ExtoleWebView
import com.extole.android.sdk.Id
import com.extole.android.sdk.Operation
import com.extole.android.sdk.ProtocolHandler
import com.extole.android.sdk.RestException
import com.extole.android.sdk.SendError
import com.extole.android.sdk.Zone
import com.extole.android.sdk.impl.app.App
import com.extole.android.sdk.impl.app.AppEvent
import com.extole.android.sdk.impl.app.ExtoleServicesImpl
import com.extole.android.sdk.impl.app.OperationImpl
import com.extole.android.sdk.impl.app.action.LoadOperationsAction
import com.extole.android.sdk.impl.app.condition.EventCondition
import com.extole.android.sdk.impl.http.AuthorizationEndpoints
import com.extole.android.sdk.randomAlphaNumericString
import com.extole.webview.ExtoleWebViewImpl
import kotlinx.coroutines.coroutineScope
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class ExtoleImpl(
    private val programDomain: String,
    val appName: String,
    val sandbox: String,
    private val context: ApplicationContext,
    private val labels: Set<String> = mutableSetOf(),
    private val data: Map<String, String> = emptyMap(),
    val appData: MutableMap<String, String> = mutableMapOf(),
    val appHeaders: MutableMap<String, String> = mutableMapOf(),
    val identifier: String? = null,
    val listenToEvents: Boolean = true,
    val additionalProtocolHandlers: List<ProtocolHandler>,
    private val configurationLoader: ((app: App, data: Map<String, Any>) -> List<Operation>)? = null,
    private val disabledActions: Set<Action.ActionType> = emptySet(),
    val jwt: String? = null
) : ExtoleInternal {

    companion object {
        private const val FLOW_CONFIGURATION_ZONE = "mobile_bootstrap"
        private const val SANDBOX_HEADER = "X-Extole-Sandbox"
        private const val APP_HEADER = "X-Extole-App"
        private const val APP_VERSION_HEADER = "X-Extole-App-Version"
        private const val APP_TYPE_HEADER = "X-Extole-App-Type"
        private const val APP_SHA_HEADER = "X-Extole-App-Sha"
        private const val USER_AGENT_HEADER = "User-Agent"
        private const val IDENTIFY_EVENT_NAME = "identify"
        private const val ACCESS_TOKEN_HEADER_NAME = "x-extole-token"
        private const val ACCESS_TOKEN = "access_token"
        private const val ACCESS_TOKEN_CHANGED_EVENT_NAME = "access_token_changed"
    }

    private var operations: MutableList<Operation> = mutableListOf()
    private var configuration: MutableList<JSONObject> = mutableListOf()
    private var zonesResponse: Zones = Zones(mutableMapOf())
    private lateinit var extoleServices: ExtoleServicesImpl
    private val flowController = App
    private var logger: ExtoleLogger = ExtoleLoggerImpl()

    private var accessToken: String? = null
    private lateinit var tokenApi: AuthorizationEndpoints

    init {
        appData["appName"] = appName
        initApiClient(identifier, jwt)
        if (listenToEvents) {
            subscribe()
        }
    }

    override suspend fun fetchZone(
        zoneName: String,
        fethZoneData: Map<String, Any?>
    ): Pair<Zone, Campaign> {
        val requestData = mutableMapOf<String, Any?>()
        requestData.putAll(fethZoneData)
        requestData.putAll(this.data)
        EventBus.getDefault().post(AppEvent(zoneName, fethZoneData))
        val campaign: Campaign?
        var zoneResponse = zonesResponse.get(zoneName)
        if (zoneResponse == null) {
            extoleServices.getZoneService()
                .getZones(setOf(zoneName), fethZoneData, labels)
                .getAll().forEach { response ->
                    response.value?.let { zonesResponse.add(response.key, it) }
                }
            zoneResponse = zonesResponse.get(zoneName)
        }

        campaign = CampaignImpl(
            zoneResponse?.campaignId!!,
            zoneResponse,
            this
        )
        return Pair(zoneResponse, campaign)
    }

    override fun getLogger(): ExtoleLogger {
        return logger
    }

    override fun getContext(): ApplicationContext = context

    override suspend fun sendEvent(
        eventName: String,
        data: Map<String, Any?>,
        jwt: String?
    ): Id<Event> {
        EventBus.getDefault().post(AppEvent(eventName, data))
        try {
            val requestData = mutableMapOf<String, Any?>()
            requestData.putAll(data)
            requestData["labels"] = labels.joinToString(",")
            val requestBody = mutableMapOf<String, Any?>()
            requestBody["event_name"] = eventName
            requestBody["data"] = requestData
            jwt?.let {
                requestBody["jwt"] = it
            }

            val httpPostResult = extoleServices.getEventsEndpoints().post(requestBody)
            val accessTokenHeader = httpPostResult.headers.entries
                .filter { it.key != null }
                .filter {
                    it.key.lowercase() == ACCESS_TOKEN_HEADER_NAME
                }.map { it.value }.flatten().first()
            if (accessTokenHeader.isNotBlank() && accessTokenHeader != accessToken) {
                clearZonesCache()
                setAccessToken(accessTokenHeader)
                EventBus.getDefault().post(AppEvent(ACCESS_TOKEN_CHANGED_EVENT_NAME))
            }
            return Id(httpPostResult.entity.getString("id"))
        } catch (exception: RestException) {
            logger.error(
                exception,
                "Unable to send event=$eventName, data=$data, labels=$labels, programDomain=$programDomain"
            )
            throw SendError(
                exception.uniqueId,
                exception.errorCode,
                exception.httpStatusCode,
                exception.message,
                exception.parameters
            )
        }
    }

    override fun logout() {
        clearAccessToken()
        clearZonesCache()
        createAccessToken()
    }

    override suspend fun identify(identifier: String, data: Map<String, String>): Id<Event> {
        val identifyData = data.toMutableMap()
        identifyData["email"] = identifier
        return sendEvent(IDENTIFY_EVENT_NAME, identifyData)
    }

    override suspend fun identifyJwt(jwt: String, data: Map<String, String>): Id<Event> {
        return sendEvent(IDENTIFY_EVENT_NAME, data, jwt)
    }

    override suspend fun clone(
        programDomain: String?,
        appName: String?,
        sandbox: String?,
        context: ApplicationContext?,
        newLabels: Set<String>?,
        extendCurrentLabels: Set<String>,
        newData: Map<String, String>?,
        extendCurrentData: Map<String, String>,
        appData: Map<String, String>?,
        appHeaders: Map<String, String>?,
        identifier: String?,
    ): Extole {
        val copyContext = context ?: this.context
        val extendedLabels = newLabels ?: this.labels
        extendedLabels.toMutableSet().addAll(extendCurrentLabels)
        val extendedData = appData?.toMutableMap() ?: this.appData
        val extendedHeaders = appHeaders?.toMutableMap() ?: this.appHeaders
        extendedData.toMutableMap().putAll(extendCurrentData)
        val extole = ExtoleImpl(
            programDomain ?: this.programDomain,
            appName ?: this.appName,
            sandbox ?: this.sandbox,
            copyContext,
            extendedLabels,
            newData?.toMutableMap() ?: this.data,
            extendedData,
            extendedHeaders,
            identifier ?: this.identifier,
            listenToEvents,
            additionalProtocolHandlers,
            configurationLoader,
            this.disabledActions
        )
        extole.refresh()
        return extole
    }

    override fun webView(
        webView: WebView,
        headers: Map<String, String>,
        data: Map<String, String>,
    ): ExtoleWebView {
        val partnerShareId = randomAlphaNumericString()
        context.getPersistence().put(PARTNER_SHARE_ID_PREFERENCES_KEY, partnerShareId)
        val mutableHeaders = headers.toMutableMap()
        mutableHeaders.put("Authorization", "Bearer $accessToken")
        val mutableData = data.toMutableMap()
        mutableData.putAll(this.data)
        mutableData.putAll(this.appData)
        return ExtoleWebViewImpl(
            programDomain,
            webView,
            context,
            this,
            mutableHeaders,
            mutableData,
            additionalProtocolHandlers
        )
    }

    suspend fun refresh(): Extole = coroutineScope {
        initApiClient()
        return@coroutineScope this@ExtoleImpl
    }

    private fun setCache(key: String, value: String) {
        context.getPersistence().put(key, value)
    }

    private fun initApiClient(identifier: String? = null, jwt: String? = null) {
        val androidLogger = ExtoleLogger.builder().build()
        androidLogger.debug("Initialized Extole for programDomain=$programDomain")
        accessToken = context.getPersistence().get(ACCESS_TOKEN_PREFERENCES_KEY)
        tokenApi = AuthorizationEndpoints(programDomain, accessToken, getHeaders())
        try {
            if (accessToken == null || identifier != null) {
                createAccessToken(identifier, jwt)
            } else {
                tokenApi.getTokenDetails(mapOf(ACCESS_TOKEN to accessToken))
            }
        } catch (e: RestException) {
            createAccessToken()
        }
        extoleServices = ExtoleServicesImpl(this)
        val extoleLogger = ExtoleLogger.builder()
            .withProgramDomain(programDomain)
            .withAccessToken(accessToken)
            .build()
        extoleLogger.debug("Access Token initialized: $accessToken")
    }

    private fun createAccessToken(identifier: String? = null, jwt: String? = null) {
        val accessToken = tokenApi.createToken(identifier, jwt).entity.getString(ACCESS_TOKEN)
        setAccessToken(accessToken)
    }

    private fun setAccessToken(accessToken: String?) {
        accessToken?.let {
            this.accessToken = accessToken
            setCache(ACCESS_TOKEN_PREFERENCES_KEY, it)
        }
    }

    private fun clearAccessToken() {
        setAccessToken("")
    }

    private fun clearZonesCache() {
        this.zonesResponse = Zones(mutableMapOf())
    }

    private fun subscribe() {
        configurationLoader?.let { operations.addAll(it(App, mapOf())) }
        if (configurationLoader == null) {
            operations.add(
                OperationImpl(
                    listOf(EventCondition(listOf("app_initialized"))),
                    listOf(LoadOperationsAction(listOf(FLOW_CONFIGURATION_ZONE)))
                )
            )
        }
        App.extole = this
        if (!EventBus.getDefault().isRegistered(flowController)) {
            EventBus.getDefault().register(flowController)
        }
        EventBus.getDefault().post(AppEvent("app_initialized"))
    }

    override fun setLogger(logger: ExtoleLogger) {
        this.logger = logger
    }

    override fun getServices(): ExtoleServicesImpl {
        return ExtoleServicesImpl(this)
    }

    override fun getHeaders(): Map<String, String> {
        val allHeaders = mutableMapOf(
            SANDBOX_HEADER to sandbox,
            USER_AGENT_HEADER to "Extole Android SDK/1.0",
            APP_HEADER to appName,
            APP_VERSION_HEADER to getApplicationVersion(),
            APP_TYPE_HEADER to "mobile_sdk_android",
            APP_SHA_HEADER to getApplicationSignature()
        )
        allHeaders.putAll(appHeaders)
        return allHeaders
    }

    fun getApplicationVersion(): String {
        return context.getAppContext().packageManager
            .getPackageInfo(context.getAppContext().packageName, 0).versionName ?: ""
    }

    fun getApplicationSignature(): String {
        val info = context.getAppContext().getPackageManager()
            .getPackageInfo(
                context.getAppContext().packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        return info.packageName
    }

    override fun getLabels() = labels

    override fun getData(): MutableMap<String, String> {
        return appData
    }

    override fun getProgramDomain() = programDomain

    override fun getAccessToken() = accessToken

    override fun getZonesResponse() = zonesResponse

    override fun getOperations() = operations

    override fun getJsonConfiguration(): MutableList<JSONObject> {
        return configuration
    }

    override fun getDisabledActions(): Set<Action.ActionType> {
        return disabledActions
    }
}
