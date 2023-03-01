package com.extole.android.sdk.impl.app

import com.extole.android.sdk.ExtoleServices
import com.extole.android.sdk.RewardService
import com.extole.android.sdk.ShareService
import com.extole.android.sdk.ZoneService
import com.extole.android.sdk.impl.ExtoleInternal
import com.extole.android.sdk.impl.ZoneServiceImpl
import com.extole.android.sdk.impl.http.EventEndpoints
import com.extole.android.sdk.impl.http.MeRewardEndpoints

class ExtoleServicesImpl(
    var extole: ExtoleInternal
) : ExtoleServices {
    private var eventsEndpoints: EventEndpoints =
        EventEndpoints(extole.getProgramDomain(), extole.getAccessToken(), extole.getHeaders())
    private var meRewardEndpoints: MeRewardEndpoints =
        MeRewardEndpoints(extole.getProgramDomain(), extole.getAccessToken(), extole.getHeaders())
    private var zoneService: ZoneServiceImpl =
        ZoneServiceImpl(
            extole
        )
    private var rewardService: RewardServiceImpl = RewardServiceImpl(meRewardEndpoints)
    private var shareService: ShareServiceImpl = ShareServiceImpl(extole)

    override fun getEventsEndpoints(): EventEndpoints = eventsEndpoints
    override fun getMeRewardEndpoints(): MeRewardEndpoints = meRewardEndpoints
    override fun getZoneService(): ZoneService = zoneService
    override fun getRewardService(): RewardService = rewardService
    override fun getShareService(): ShareService = shareService
}
