package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * @param sd          True if: for SD-JWT issuance: field is selectively disclosable, for SD-JWT presentation: field should be disclosed
 * @param children    Not null, if field is an object, contains SDField map for the properties of the object
 */
data class SDField(
  val sd: Boolean,
  val children: Map<String, SDField>? = null
) {
  companion object {
    fun prettyPrintSdMap(sdMap: Map<String, SDField>, indentBy: Int = 0) {
      val indentation = (0).rangeTo(indentBy).joinToString (" "){ "" }
      sdMap.keys.forEach { key ->
        println("${indentation}- $key: ${sdMap[key]?.sd == true}")
        sdMap[key]?.children?.also { prettyPrintSdMap(it, indentBy+2) }
      }
    }

    fun generateSdMap(fullPayload: JsonObject, undisclosedPayload: JsonObject): Map<String, SDField> {
      return fullPayload.mapValues { entry ->
        if(!undisclosedPayload.containsKey(entry.key))
          SDField(true)
        else if(entry.value is JsonObject && undisclosedPayload[entry.key] is JsonObject) {
          SDField(false, generateSdMap(entry.value.jsonObject, undisclosedPayload[entry.key]!!.jsonObject))
        } else {
          SDField(false)
        }
      }
    }
  }
}
