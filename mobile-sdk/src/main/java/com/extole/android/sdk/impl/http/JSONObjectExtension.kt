package com.extole.android.sdk.impl.http

import org.json.JSONArray
import org.json.JSONObject

fun toMap(jsonobj: JSONObject): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    val keys = jsonobj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        var value = jsonobj[key]
        if (value is JSONArray) {
            value = toList(value)
        } else if (value is JSONObject) {
            value = toMap(value)
        }
        map[key] = value
    }
    return map
}

private fun toList(array: JSONArray): List<Any> {
    val list: MutableList<Any> = ArrayList()
    for (i in 0 until array.length()) {
        var value = array[i]
        if (value is JSONArray) {
            value = toList(value)
        } else if (value is JSONObject) {
            value = toMap(value)
        }
        list.add(value)
    }
    return list
}
