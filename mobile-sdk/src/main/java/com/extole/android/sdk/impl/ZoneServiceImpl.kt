package com.extole.android.sdk.impl

import com.extole.android.sdk.ExtoleLogger
import com.extole.android.sdk.Id
import com.extole.android.sdk.RestException
import com.extole.android.sdk.Zone
import com.extole.android.sdk.ZoneService
import com.extole.android.sdk.impl.http.ZonesEndpoints
import com.extole.android.sdk.impl.http.toMap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ZoneServiceImpl(val extole: ExtoleInternal) :
    ZoneService {

    val programDomain: String = extole.getProgramDomain()
    val logger: ExtoleLogger = extole.getLogger()

    @Throws(RestException::class)
    override suspend fun getZones(
        zonesName: Set<String>,
        data: Map<String, Any?>,
        programLabels: Set<String>,
    ): Zones = coroutineScope {
        val prefetchedResponses = mutableMapOf<ZoneResponseKey, Zone?>()
        val zoneResponses = zonesName.map { zoneName ->
            async { getZone(zoneName, data) }
        }.awaitAll()

        zoneResponses.forEach { zoneResponse ->
            prefetchedResponses[ZoneResponseKey(zoneResponse.getName(), data)] = zoneResponse
        }
        return@coroutineScope Zones(prefetchedResponses.filter { it.value != null }
            .toMutableMap())
    }

    @Throws(RestException::class)
    private fun getZone(
        zoneName: String,
        data: Map<String, Any?>
    ): Zone {
        val zonesApi = ZonesEndpoints(programDomain, extole.getAccessToken(), extole.getHeaders())
        logger.debug("Rendering zone=$zoneName, data=$data")
        val response = zonesApi.render(zoneName, data)
        val campaignId = response.entity.getString("campaign_id")
        return Zone(
            zoneName,
            Id(campaignId),
            toMap(response.entity.getJSONObject("data")),
            extole
        )
    }
}
