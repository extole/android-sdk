package com.extole.android.sdk

data class Zone(
    private val zoneName: String,
    val campaignId: Id<Campaign>,
    val content: Map<String, Any?>?,
    val extole: Extole
) {
    fun getName(): String {
        return zoneName
    }

    fun get(dottedPath: String): Any? {
        if (dottedPath.contains(".")) {
            return getFlatten(dottedPath)
        }
        return content?.get(dottedPath)
    }

    suspend fun tap(): Id<Event> {
        return extole.sendEvent(zoneName + "_tap", mapOf("target" to "campaign_id:$campaignId"))
    }

    suspend fun viewed(): Id<Event> {
        return extole.sendEvent(zoneName + "_viewed", mapOf("target" to "campaign_id:$campaignId"))
    }

    private fun getFlatten(dottedPath: String): Any? {
        var initialReference = content
        val path = dottedPath.split(".")
        for (i in 0..path.size - 2) {
            initialReference = initialReference?.get(path[i]) as Map<String, Any?>?
        }
        if (initialReference != null) {
            return initialReference.get(path[path.size - 1])
        }
        return null
    }
}
