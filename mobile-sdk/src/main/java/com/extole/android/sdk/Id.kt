package com.extole.android.sdk

data class Id<T>(val id: String?) {
    fun getValue() = id

    override fun toString(): String {
        return id ?: ""
    }
}
