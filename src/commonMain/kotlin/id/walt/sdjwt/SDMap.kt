package id.walt.sdjwt

import kotlinx.serialization.json.*

/**
 * @param decoyMode Generate decoy digests for this hierarchical level randomly or fixed, set to NONE for parsed SD-JWTs
 * @param decoys  Num (fixed mode) or max num (random mode) of decoy digests to add for this hierarchical level. 0 if NONE.
 */
class SDMap(
  private val fields: Map<String, SDField>,
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
    return keys.map { key ->
      "- $key: ${fields[key]?.sd == true}\n${fields[key]?.children?.prettyPrint(indentBy+2) ?: ""}"
    }.joinToString("\n")
  }

  companion object {

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

fun Map<String, SDField>.toSDMap(decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0): SDMap {
  return SDMap(this, decoyMode, decoys)
}