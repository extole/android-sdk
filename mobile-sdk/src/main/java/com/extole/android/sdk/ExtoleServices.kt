package com.extole.android.sdk

import com.extole.android.sdk.impl.http.EventEndpoints
import com.extole.android.sdk.impl.http.MeRewardEndpoints

interface ExtoleServices {

    /**
     * [EventEndpoints] it's an low-level service used to send events to Extole
     */
    fun getEventsEndpoints(): EventEndpoints

    /**
     * [MeRewardEndpoints] it's an low-level service used to get person's rewards
     */
    fun getMeRewardEndpoints(): MeRewardEndpoints

    /**
     * [ZoneService] it's an low-level service used to fetch multiple zones, if
     */
    fun getZoneService(): ZoneService

    /**
     * [RewardService] is used to poll a reward
     */
    fun getRewardService(): RewardService

    /**
     * [ShareService] is used to send a share event
     */
    fun getShareService(): ShareService
}
