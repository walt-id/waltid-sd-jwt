package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject

actual class JWTFactory {
  actual fun sign(
    payload: JsonObject
  ): String {
    TODO("Not yet implemented")
  }
}