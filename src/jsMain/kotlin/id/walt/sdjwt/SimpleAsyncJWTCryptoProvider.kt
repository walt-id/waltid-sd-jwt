package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Expected default implementation for JWTCryptoProvider on each platform
 * Implemented in platform specific modules
 * @see JWTCryptoProvider
 */
open class SimpleAsyncJWTCryptoProvider(
  private val algorithm: String,
  private val keyParam: dynamic,
  private val options: dynamic
) : AsyncJWTCryptoProvider {
  override suspend fun sign(payload: JsonObject, keyID: String?): String = suspendCoroutine { continuation ->
    console.log("SIGNING", payload.toString())
    jose.SignJWT(JSON.parse(payload.toString())).setProtectedHeader(buildJsonObject {
      put("alg", algorithm)
      put("typ", "JWT")
      keyID?.also { put("kid", it) }
    }.let { JSON.parse(it.toString()) }).sign(keyParam, options).then({
      console.log("SIGNED")
      continuation.resume(it)
    }, {
      console.log("ERROR SIGNING", it.message)
    })
  }

  override suspend fun verify(jwt: String): Boolean = suspendCoroutine { continuation ->
    console.log("Verifying JWT: $jwt")
    jose.jwtVerify(jwt, keyParam, options ?: js("{}")).then (
      {
        console.log("Verified.")
        continuation.resume(true)
      },
      {
        console.log("Verification failed: ${it.message}")
        continuation.resume(false)
      }
    )
  }
}