package com.extole.android.sdk

import kotlinx.coroutines.Deferred

interface ShareResponse {

    fun getPartnerShareId(): String
    fun getEventId(): Deferred<Id<Event>>
}
