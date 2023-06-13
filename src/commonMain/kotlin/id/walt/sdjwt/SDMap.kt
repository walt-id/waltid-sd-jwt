package id.walt.sdjwt

import kotlinx.serialization.json.*
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Selective disclosure map, that describes for each payload field recursively, whether it should be selectively disclosable / selected for disclosure.
 * @param fields  map of field keys to SD field descriptors
 * @param decoyMode **For SD-JWT issuance:** Generate decoy digests for this hierarchical level randomly or fixed, set to NONE for parsed SD-JWTs, **for presentation:** _unused_
 * @param decoys  **For SD-JWT issuance:** Num (fixed mode) or max num (random mode) of decoy digests to add for this hierarchical level. 0 if NONE, **for presentation:** _unused_
 */
@ExperimentalJsExport
@JsExport
class SDMap(
  val fields: Map<String, SDField>,
  val decoyMode: DecoyMode = DecoyMode.NONE,
  val decoys: Int = 0
  ): Map<String, SDField> {
  override val entries: Set<Map.Entry<String, SDField>>
    get() = fields.entries
  override val keys: Set<String>
    get() = fields.keys
  override val size: Int
    get() = fields.size
  override val values: Collection<SDField>
    get() = fields.values

  override fun isEmpty() = fields.isEmpty()

  override fun get(key: String) = fields[key]

  override fun containsValue(value: SDField) = fields.containsValue(value)

  override fun containsKey(key: String) = fields.containsKey(key)

  fun prettyPrint(indentBy: Int = 0): String {
    val indentation = (0).rangeTo(indentBy).joinToString (" "){ "" }
    return "${indentation} + with decoys: ${decoyMode} (${decoys})\n" + keys.flatMap { key ->
      listOfNotNull(
        "${indentation}- $key: ${fields[key]?.sd == true}",
        fields[key]?.children?.prettyPrint(indentBy+2)
      )
    }.joinToString("\n")
  }

  companion object {

    /**
     * Generate SDMap by comparing the fully disclosed payload with the undisclosed payload
     * @param fullPayload Full payload containing all fields
     * @param undisclosedPayload  Payload with selectively disclosable fields removed
     * @param decoyMode **For SD-JWT issuance:** Generate decoy digests for this hierarchical level randomly or fixed, set to NONE for parsed SD-JWTs, **for presentation:** _unused_
     * @param decoys  **For SD-JWT issuance:** Num (fixed mode) or max num (random mode) of decoy digests to add for this hierarchical level. 0 if NONE, **for presentation:** _unused_.
     */
    fun generateSDMap(fullPayload: JsonObject, undisclosedPayload: JsonObject, decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0): SDMap {
      return fullPayload.mapValues { entry ->
        if(!undisclosedPayload.containsKey(entry.key))
          SDField(true)
        else if(entry.value is JsonObject && undisclosedPayload[entry.key] is JsonObject) {
          SDField(false, generateSDMap(entry.value.jsonObject, undisclosedPayload[entry.key]!!.jsonObject, decoyMode, decoys))
        } else {
          SDField(false)
        }
      }.let { it.toSDMap(decoyMode, decoys) }
    }

    /**
     * Generate SDMap based on set of simplified JSON paths
     * @param jsonPaths Simplified JSON paths, of fields that should be selectively disclosable. e.g.: "credentialSubject.firstName", "credentialSubject.dateOfBirth"
     * @param decoyMode **For SD-JWT issuance:** Generate decoy digests for this hierarchical level randomly or fixed, set to NONE for parsed SD-JWTs, **for presentation:** _unused_
     * @param decoys  **For SD-JWT issuance:** Num (fixed mode) or max num (random mode) of decoy digests to add for this hierarchical level. 0 if NONE, **for presentation:** _unused_.
     */
    @JsName("generateSDMapFromJsonPaths")
    fun generateSDMap(jsonPaths: Collection<String>, decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0): SDMap {
      val pathMap = jsonPaths.map { path -> Pair(path.substringBefore("."), path.substringAfter(".", "")) }
        .groupBy({ p -> p.first }, { p -> p.second }).mapValues { entry -> entry.value.filterNot { it.isEmpty() } }
      return pathMap.mapValues {
        SDField(
          true, if (it.value.isNotEmpty()) {
            generateSDMap(it.value, decoyMode, decoys)
          } else null
        )
      }.toSDMap(decoyMode, decoys)
    }

    private fun regenerateSDField(sd: Boolean, value: JsonElement, digestedDisclosure: Map<String, SDisclosure>): SDField {
      return SDField(sd, if(value is JsonObject) {
        regenerateSDMap(value.jsonObject, digestedDisclosure)
      } else null)
    }

    /**
     * Regenerate SDMap recursively, from undisclosed payload and digested disclosures map. Used for parsing SD-JWTs.
     * @param undisclosedPayload  Undisclosed payload as contained in the JWT body of the SD-JWT token.
     * @param digestedDisclosures Map of digests to disclosures appended to the JWT in the SD-JWT token
     */
    fun regenerateSDMap(undisclosedPayload: JsonObject, digestedDisclosures: Map<String, SDisclosure>): SDMap {
      return (undisclosedPayload[SDJwt.DIGESTS_KEY]?.jsonArray?.filter { digestedDisclosures.containsKey(it.jsonPrimitive.content) }?.map {
        sdEntry -> digestedDisclosures[sdEntry.jsonPrimitive.content]!!
      }?.associateBy({it.key}, { regenerateSDField(true, it.value, digestedDisclosures) }) ?: mapOf())
        .plus(
          undisclosedPayload.filterNot { it.key == SDJwt.DIGESTS_KEY }.mapValues {
            regenerateSDField(false, it.value, digestedDisclosures)
          }
        ).toSDMap()
    }
  }

}

/**
 * Convert a Map<String, SDField> to SDMap object, with the given optional decoy parameters
 * @param decoyMode **For SD-JWT issuance:** Generate decoy digests for this hierarchical level randomly or fixed, set to NONE for parsed SD-JWTs, **for presentation:** _unused_
 * @param decoys  **For SD-JWT issuance:** Num (fixed mode) or max num (random mode) of decoy digests to add for this hierarchical level. 0 if NONE, **for presentation:** _unused_.
 */
fun Map<String, SDField>.toSDMap(decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0): SDMap {
  return SDMap(this, decoyMode, decoys)
}