package id.walt.sdjwt

import korlibs.crypto.encoding.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class SDisclosure(
  val disclosure: String,
  val salt: String,
  val key: String,
  val value: JsonElement
) {
  companion object {
    fun parse(disclosure: String) = Json.parseToJsonElement(Base64.decode(disclosure, url = true).decodeToString()).jsonArray.let {
      if(it.size != 3) {
        throw Exception("Invalid selective disclosure")
      }
      SDisclosure(disclosure, it[0].jsonPrimitive.content, it[1].jsonPrimitive.content, it[2])
    }
  }
}
