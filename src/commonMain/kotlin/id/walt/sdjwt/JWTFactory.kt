package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject

expect class JWTFactory {
  fun sign(payload: JsonObject): String
}

