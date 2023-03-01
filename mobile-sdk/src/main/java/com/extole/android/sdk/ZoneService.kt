package com.extole.android.sdk

import com.extole.android.sdk.impl.Zones

interface ZoneService {
    @Throws(RestException::class)
    suspend fun getZones(
        zonesName: Set<String>,
        data: Map<String, Any?>,
        programLabels: Set<String>
    ): Zones
}
