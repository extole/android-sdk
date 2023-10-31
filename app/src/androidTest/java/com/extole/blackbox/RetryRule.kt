package com.extole.blackbox

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RetryRule(val retryCount: Int = 3) : TestRule {

    private val TAG = RetryRule::class.java.simpleName

    override fun apply(base: Statement, description: Description): Statement {
        return statement(base, description)
    }

    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {

            override fun evaluate() {
                Log.d(TAG, "Evaluating ${description.methodName}")
                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (exception: Throwable) {
                        caughtThrowable = exception
                        Log.e(TAG, description.methodName + ": run " + (i + 1) + " failed")
                    }
                }

                Log.e(TAG, description.methodName + ": giving up after " + retryCount + " failures")
                if (caughtThrowable != null)
                    throw caughtThrowable
            }
        }
    }
}