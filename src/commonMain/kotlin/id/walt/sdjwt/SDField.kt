package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * @param sd          True if: for SD-JWT issuance: field is selectively disclosable, for SD-JWT presentation: field should be disclosed
 * @param children    Not null, if field is an object, contains SDField map for the properties of the object
 */
data class SDField(
  val sd: Boolean,
  val children: SDMap? = null
)
