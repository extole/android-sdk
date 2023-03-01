package com.extole.android.sdk

/**
 * Javascript interface that is exposed in the [ExtoleWebView] context and is used by creatives
 * to display the native sharing dialog
 * Example:
 * if (extoleShare != undefined) {
 *    extoleShare.share({"url":"", "title":"", "text":""});
 * }
 */
interface JsExtoleShare {

    /**
     * Called from JavaScript code that is executed in the [ExtoleWebView] context
     * @param jsonData - is a JSON object that contains at least an `url, `title` and `text`
     */
    fun share(jsonData: String?)
}
