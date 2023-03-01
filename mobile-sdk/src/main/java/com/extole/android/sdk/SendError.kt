package com.extole.android.sdk

class SendError(
    override val uniqueId: String,
    override val errorCode: String,
    override val httpStatusCode: String,
    override val message: String,
    override val parameters: Map<String, Any>
) : RestException(uniqueId, errorCode, httpStatusCode, message, parameters)
