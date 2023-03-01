package com.extole.mobile

import android.app.Application
import io.branch.referral.Branch

class ExtoleDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Branch.enableLogging()
        Branch.getAutoInstance(this)
    }
}
