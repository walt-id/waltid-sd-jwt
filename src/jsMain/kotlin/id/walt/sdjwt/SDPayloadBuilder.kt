package id.walt.sdjwt

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@ExperimentalJsExport
@JsExport
class SDPayloadBuilder(
  val fullPayload: dynamic
) {
  fun buildForUndisclosedPayload(undisclosedSDPayload: dynamic): SDPayload {
    return SDPayload.createSDPayload(
      Json.parseToJsonElement(JSON.stringify(fullPayload)).jsonObject,
      Json.parseToJsonElement(JSON.stringify(undisclosedSDPayload)).jsonObject)
  }

  fun buildForSDMap(sdMap: SDMap): SDPayload {
    return SDPayload.createSDPayload(
      Json.parseToJsonElement(JSON.stringify(fullPayload)).jsonObject,
      sdMap
    )
  }
}