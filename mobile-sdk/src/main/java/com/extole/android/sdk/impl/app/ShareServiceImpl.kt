package com.extole.android.sdk.impl.app

import com.extole.android.sdk.Event
import com.extole.android.sdk.Extole
import com.extole.android.sdk.Id
import com.extole.android.sdk.ShareResponse
import com.extole.android.sdk.ShareService
import com.extole.android.sdk.impl.ShareResponseImpl
import com.extole.android.sdk.randomAlphaNumericString
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ShareServiceImpl(val extole: Extole) : ShareService {

    override suspend fun emailShare(
        recipient: String,
        subject: String,
        message: String,
        data: Map<String, Any?>,
    ): Id<Event> {
        val requestData = mutableMapOf<String, Any?>()
        requestData.putAll(data)
        requestData["share.recipient"] = recipient
        requestData["share.subject"] = subject
        requestData["share.message"] = message
        requestData["share.channel"] = "EMAIL"

        return extole.sendEvent("share", requestData)
    }

    override suspend fun sendShareEvent(
        channel: String,
        data: Map<String, Any?>,
        partnerShareId: String?
    ): ShareResponse = coroutineScope {
        var actualPartnerShareId = partnerShareId
        if (actualPartnerShareId.isNullOrEmpty()) {
            actualPartnerShareId = randomAlphaNumericString()
        }
        val requestData = mutableMapOf<String, Any?>()
        requestData.putAll(data)
        requestData["share.channel"] = channel
        requestData["partner_share_id"] = actualPartnerShareId
        return@coroutineScope ShareResponseImpl(
            actualPartnerShareId,
            async { extole.sendEvent("share", requestData) })
    }
}
