package com.extole.android.sdk

import org.json.JSONObject

interface RewardService {

    /**
     * When current customer receive an reward, this method can be used to poll for that reward
     * @param pollingId - the polling id for the reward
     * @param timeoutSeconds - polling timeout in seconds
     * @param retries - number for retries that will be done for receiving the reward
     * @return [JSONObject] - JSON representation for the zone response
     * @throws [RestException]
     */
    @Throws(RestException::class)
    suspend fun pollReward(
        pollingId: String,
        timeoutSeconds: Long = 5,
        retries: Int = 5
    ): JSONObject?
}
