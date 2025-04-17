package com.extole.android.sdk.impl

import com.extole.android.sdk.Zone

data class Zones(private var zoneResponses: MutableMap<ZoneResponseKey, Zone?>) {

    fun get(zoneName: String, data: Map<String, Any?>): Zone? {
        return zoneResponses[ZoneResponseKey(zoneName, data)]
    }

    fun add(zoneKey: ZoneResponseKey, zone: Zone) {
        zoneResponses[zoneKey] = zone
    }

    fun getAll(): Map<ZoneResponseKey, Zone?> = zoneResponses
}

data class ZoneResponseKey(val zoneName: String, val data: Map<String, Any?>)
