package com.extole.android.sdk.impl

import com.extole.android.sdk.Zone

data class Zones(private var zoneResponses: MutableMap<ZoneResponseKey, Zone?>) {

    fun get(zoneName: String): Zone? {
        return zoneResponses[ZoneResponseKey(zoneName)]
    }

    fun add(zoneKey: ZoneResponseKey, zone: Zone) {
        zoneResponses[zoneKey] = zone
    }

    fun getAll(): Map<ZoneResponseKey, Zone?> = zoneResponses
}

data class ZoneResponseKey(val zoneName: String)
