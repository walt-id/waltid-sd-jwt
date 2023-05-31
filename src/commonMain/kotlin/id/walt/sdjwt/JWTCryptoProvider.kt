package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject

interface JWTCryptoProvider {
  fun sign(payload: JsonObject, keyID: String? = null): String
  fun verify(jwt: String): Boolean
}

expect class SimpleJWTCryptoProvider : JWTCryptoProvider