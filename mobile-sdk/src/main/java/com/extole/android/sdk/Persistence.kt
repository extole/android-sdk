package com.extole.android.sdk

interface Persistence<KEY_TYPE, VALUE_TYPE> {

    fun put(key: KEY_TYPE, value: VALUE_TYPE)

    fun get(key: KEY_TYPE): String?

    fun delete(key: KEY_TYPE)
}
