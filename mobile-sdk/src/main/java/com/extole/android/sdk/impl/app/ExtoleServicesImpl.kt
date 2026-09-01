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
    private var zoneService: ZoneServiceImpl =
        ZoneServiceImpl(
            extole
        )
    private var shareService: ShareServiceImpl = ShareServiceImpl(extole)

    override fun getEventsEndpoints(): EventEndpoints =
        EventEndpoints(extole.getProgramDomain(), extole.getAccessToken(), extole.getHeaders())

    override fun getMeRewardEndpoints(): MeRewardEndpoints =
        MeRewardEndpoints(extole.getProgramDomain(), extole.getAccessToken(), extole.getHeaders())

    override fun getZoneService(): ZoneService = zoneService
    override fun getRewardService(): RewardService = RewardServiceImpl(getMeRewardEndpoints())
    override fun getShareService(): ShareService = shareService
}
