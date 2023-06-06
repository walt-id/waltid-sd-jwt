package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Selective disclosure information for a given payload field
 * @param sd          **Issuance:** field is made selectively disclosable if *true*, **Presentation:** field should be _disclosed_ if *true*, or _undisclosed_ if *false*
 * @param children    Not null, if field is an object. Contains SDMap for the properties of the object
 * @see SDMap
 */
data class SDField(
  val sd: Boolean,
  val children: SDMap? = null
)
