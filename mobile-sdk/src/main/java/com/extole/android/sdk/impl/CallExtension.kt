package com.extole.android.sdk

fun randomAlphaNumericString(desiredStrLength: Int = 15): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    return (1..desiredStrLength)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}
