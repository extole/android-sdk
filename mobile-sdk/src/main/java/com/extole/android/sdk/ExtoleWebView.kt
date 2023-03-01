package com.extole.android.sdk

/**
 * Fetches the specified zone into the WebView leveraging the current
 * extole context (data, labels etc) to make the zone request, zone will be loaded as a get request
 * access_token will be passed as a header
 * @see [ExtoleWebView]
 */
interface ExtoleWebView {

    /**
     * Used to load a zone by name
     * @param zone - the name of the zone that should be loaded
     */
    fun load(zone: String)
}
