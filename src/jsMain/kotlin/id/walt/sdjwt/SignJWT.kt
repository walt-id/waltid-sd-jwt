package id.walt.sdjwt

import kotlin.js.Promise

@JsModule("jose")
@JsNonModule
external object jose {
  class SignJWT(payload: dynamic) {
    fun setProtectedHeader(protectedHeader: dynamic): SignJWT
    fun setIssuer(issuer: String?): SignJWT
    fun setSubject(subject: String?): SignJWT
    fun setAudience(audience: String?): SignJWT
    fun setAudience(audience: Array<String>?): SignJWT
    fun setJti(jwtId: String?): SignJWT
    fun setNotBefore(input: Number?): SignJWT
    fun setNotBefore(input: String?): SignJWT
    fun setExpirationTime(input: Number?): SignJWT
    fun setExpirationTime(input: String?): SignJWT
    fun setIssuedAt(input: Number?): SignJWT

    fun sign(key: dynamic, options: dynamic): Promise<String>
  }
}