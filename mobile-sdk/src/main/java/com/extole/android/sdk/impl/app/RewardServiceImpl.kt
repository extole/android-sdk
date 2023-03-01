package com.extole.android.sdk.impl.app

import com.extole.android.sdk.RewardService
import com.extole.android.sdk.impl.http.MeRewardEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RewardServiceImpl(val meRewardEndpoints: MeRewardEndpoints) : RewardService {

    override suspend fun pollReward(
        pollingId: String,
        timeoutSeconds: Long,
        retries: Int
    ): JSONObject? =
        withContext(Dispatchers.IO) {
            (0..retries).forEach { _ ->
                val rewardResponse =
                    meRewardEndpoints
                        .getRewardStatus(pollingId, null, null)
                if (rewardResponse.entity.getString("status") != "PENDING") {
                    return@withContext rewardResponse.entity
                }
                delay(TimeUnit.SECONDS.toMillis(timeoutSeconds))
            }
            return@withContext null
        }
}
