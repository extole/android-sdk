package com.extole.android.sdk

open class RestException(
    open val uniqueId: String,
    open val httpStatusCode: String,
    open val errorCode: String,
    open override val message: String,
    open val parameters: Map<String, Any> = emptyMap()
) : RuntimeException()
