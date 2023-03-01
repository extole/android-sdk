package com.extole.android.sdk

interface ShareService {
    /**
     * Used to send an email share
     * @param recipient - the email of the recipient
     * @param subject - the subject used for the email
     * @param message - the body for the email
     * @param data - a dictionary with data that will be used when rendering email creative
     */
    suspend fun emailShare(
        recipient: String,
        subject: String,
        message: String,
        data: Map<String, Any?> = emptyMap()
    ): Id<Event>

    /**
     * Used to send a share event
     * @param channel represents the way the share was done (ex: FACEBOOK, TWITTER, EMAIL...)
     * @param data a dictionary with data
     * @param partnerShareId your shareId, that can be different from the shareId that Extole uses
     */
    @Throws(SendError::class)
    suspend fun sendShareEvent(
        channel: String,
        data: Map<String, Any?> = emptyMap(),
        partnerShareId: String? = null,
    ): ShareResponse
}
