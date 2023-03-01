package com.extole.android.sdk.impl

import android.content.Context
import android.content.SharedPreferences
import com.extole.android.sdk.Persistence

class SharedPreferencesPersistence(context: Context) : Persistence<String, String> {
    val sharedPreferences: SharedPreferences? =
        context.getSharedPreferences("extole-data", Context.MODE_PRIVATE)

    override fun put(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    override fun get(key: String): String? {
        return sharedPreferences?.getString(key, null)
    }

    override fun delete(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }
}
