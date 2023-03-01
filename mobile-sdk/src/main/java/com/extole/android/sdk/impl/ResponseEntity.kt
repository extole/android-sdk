package com.extole.android.sdk.impl

class ResponseEntity<T>(
    val entity: T,
    val headers: Map<String, Collection<String>>,
    val code: Int
)
