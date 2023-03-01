package com.extole.blackbox

data class RestException(
    val uniqueId: String,
    val httpStatusCode: Int?,
    val errorCode: String?,
    override val message: String,
    val parameters: Map<String, Any> = emptyMap()
) : RuntimeException()
