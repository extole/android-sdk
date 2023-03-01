package com.extole.blackbox

class BlackboxNameGenerator {

    fun getName(prefix: String): String {
        return prefix + "-" + System.nanoTime().toString()
    }

    fun getEmailAddress(prefix: String = "p"): String {
        val generatedEmail = prefix + "-" + System.nanoTime().toString() + "." + EMAIL_DOMAIN
        if (generatedEmail.length > MAX_EMAIL_LENGTH) {
            throw Exception("Email prefix is too long:$prefix")
        }
        return generatedEmail
    }

    companion object {
        private const val MAX_EMAIL_LENGTH = 220
        private val MAILBOX_ID: String = "gezt5tev"
        private val EMAIL_DOMAIN = MAILBOX_ID + "@mailosaur.io"
    }
}
