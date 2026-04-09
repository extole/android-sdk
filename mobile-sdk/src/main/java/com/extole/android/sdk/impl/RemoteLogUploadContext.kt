package com.extole.android.sdk.impl

internal object RemoteLogUploadContext {
    private val suppressRemoteUpload = ThreadLocal<Boolean>()

    inline fun <T> withSuppressedRemoteUpload(whenSuppressed: Boolean, block: () -> T): T {
        if (!whenSuppressed) {
            return block()
        }

        val previousValue = suppressRemoteUpload.get()
        suppressRemoteUpload.set(true)

        return try {
            block()
        } finally {
            if (previousValue == null) {
                suppressRemoteUpload.remove()
            } else {
                suppressRemoteUpload.set(previousValue)
            }
        }
    }

    fun shouldSuppressRemoteUpload(): Boolean = suppressRemoteUpload.get() == true
}
