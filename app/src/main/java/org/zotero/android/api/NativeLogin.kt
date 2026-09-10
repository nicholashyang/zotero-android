package org.zotero.android.api

/** Deliberately not a data class: credentials must never appear in generated toString(). */
class NativeLoginRequest(val username: String, val password: String) {
    val name = "Zotero Android"
    val access = mapOf(
        "user" to mapOf("library" to true, "notes" to true, "write" to true, "files" to true),
        "groups" to mapOf("all" to mapOf("library" to true, "write" to true)),
    )
}
class NativeLoginResponse(val key: String?, val userID: Long?, val username: String?) {
    fun isValid() = !key.isNullOrBlank() && (userID ?: 0) > 0 && !username.isNullOrBlank()
}
