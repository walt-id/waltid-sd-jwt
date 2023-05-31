package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject

actual class SimpleJWTCryptoProvider : JWTCryptoProvider {
  override fun sign(payload: JsonObject, keyID: String?): String {
    TODO("Not yet implemented")
  }

  override fun verify(jwt: String): Boolean {
    TODO("Not yet implemented")
  }
}