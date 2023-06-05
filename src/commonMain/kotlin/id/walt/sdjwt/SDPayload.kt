package id.walt.sdjwt

import korlibs.crypto.SecureRandom
import korlibs.crypto.encoding.Base64
import korlibs.crypto.encoding.base64Url
import korlibs.crypto.sha256
import kotlinx.serialization.json.*

data class SDPayload (
  val undisclosedPayload: JsonObject,
  val digestedDisclosures: Map<String, SDisclosure> = mapOf(),
) {
  val sDisclosures
    get() = digestedDisclosures.values

  val fullPayload
    get() = disclosePayloadRecursively(undisclosedPayload, null)

  val sdMap
    get() = SDMap.regenerateSDMap(undisclosedPayload, digestedDisclosures)

  private fun disclosePayloadRecursively(payload: JsonObject, verificationDisclosureMap: MutableMap<String, SDisclosure>?): JsonObject {
    return buildJsonObject {
      payload.forEach { entry ->
        if(entry.key == SDJwt.DIGESTS_KEY) {
          if(entry.value !is JsonArray) throw Exception("SD-JWT contains invalid ${SDJwt.DIGESTS_KEY} element")
          entry.value.jsonArray.forEach {
            unveilDisclosureIfPresent(it.jsonPrimitive.content, this, verificationDisclosureMap)
          }
        } else if(entry.value is JsonObject) {
          put(entry.key, disclosePayloadRecursively(entry.value.jsonObject, verificationDisclosureMap))
        } else {
          put(entry.key, entry.value)
        }
      }
    }
  }

  private fun unveilDisclosureIfPresent(digest: String, objectBuilder: JsonObjectBuilder, verificationDisclosureMap: MutableMap<String, SDisclosure>?) {
    val sDisclosure = verificationDisclosureMap?.remove(digest) ?: digestedDisclosures[digest]
    if(sDisclosure != null) {
      objectBuilder.put(sDisclosure.key,
        if(sDisclosure.value is JsonObject) {
          disclosePayloadRecursively(sDisclosure.value.jsonObject, verificationDisclosureMap)
        } else sDisclosure.value
      )
    }
  }

  private fun filterDisclosures(currPayloadObject: JsonObject, sdMap: Map<String, SDField>) : Set<String> {
    if(currPayloadObject.containsKey(SDJwt.DIGESTS_KEY) && currPayloadObject[SDJwt.DIGESTS_KEY] !is JsonArray) {
      throw Exception("Invalid ${SDJwt.DIGESTS_KEY} format found")
    }

    return currPayloadObject.filter { entry -> entry.value is JsonObject && !sdMap[entry.key]?.children.isNullOrEmpty()}.flatMap { entry ->
      filterDisclosures(entry.value.jsonObject, sdMap[entry.key]!!.children!!)
    }.plus(
      currPayloadObject[SDJwt.DIGESTS_KEY]?.jsonArray
        ?.map { it.jsonPrimitive.content }
        ?.filter { digest -> digestedDisclosures.containsKey(digest) }
        ?.map { digest -> digestedDisclosures[digest]!! }
        ?.filter {sd -> sdMap[sd.key]?.sd == true }
        ?.flatMap { sd ->
          listOf(sd.disclosure).plus(
            if(sd.value is JsonObject && !sdMap[sd.key]?.children.isNullOrEmpty()) {
              filterDisclosures(sd.value, sdMap[sd.key]!!.children!!)
            } else listOf()
          )
        } ?: listOf()
    ).toSet()
  }

  fun withSelectiveDisclosures(sdMap: Map<String, SDField>): SDPayload {
    val selectedDisclosures = filterDisclosures(undisclosedPayload, sdMap)
    return SDPayload(undisclosedPayload, digestedDisclosures.filterValues { selectedDisclosures.contains(it.disclosure) })
  }

  fun withoutDisclosures(): SDPayload {
    return SDPayload(undisclosedPayload, mapOf())
  }

  fun verifyDisclosures() = digestedDisclosures.toMutableMap().also {
    disclosePayloadRecursively(undisclosedPayload, it)
  }.isEmpty()

  companion object {

    private fun digest(value: String): String {
      val messageDigest = value.encodeToByteArray().sha256()
      return messageDigest.base64Url
    }

    private fun generateSalt(): String {
      val randomness = SecureRandom.nextBytes(16)
      return Base64.encode(randomness, url = true)
    }

    private fun generateDisclosure(key: String, value: JsonElement): SDisclosure {
      val salt = generateSalt()
      return Base64.encode(buildJsonArray {
        add(salt)
        add(key)
        add(value)
      }.toString().encodeToByteArray(), url = true).let { disclosure ->
        SDisclosure(disclosure, salt, key, value)
      }
    }

    private fun digestSDClaim(key: String, value: JsonElement, digests2disclosures: MutableMap<String, SDisclosure>): String {
      val disclosure = generateDisclosure(key, value)
      return digest(disclosure.disclosure).also {
        digests2disclosures[it] = disclosure
      }
    }

    private fun removeSDFields(payload: JsonObject, sdMap: Map<String, SDField>): JsonObject {
      return JsonObject(payload.filterKeys { key -> sdMap[key]?.sd != true }.mapValues { entry ->
        if (entry.value is JsonObject && !sdMap[entry.key]?.children.isNullOrEmpty()) {
          removeSDFields(entry.value.jsonObject, sdMap[entry.key]?.children ?: mapOf())
        } else {
          entry.value
        }
      })
    }

    private fun generateSDPayload(payload: JsonObject, sdMap: SDMap, digests2disclosures: MutableMap<String, SDisclosure>): JsonObject {
      val sdPayload = removeSDFields(payload, sdMap).toMutableMap()
      val digests = payload.filterKeys { key ->
        // iterate over all fields that are selectively disclosable AND/OR have nested fields that might be:
        sdMap[key]?.sd == true || !sdMap[key]?.children.isNullOrEmpty()
      }.map { entry ->
        if(entry.value !is JsonObject || sdMap[entry.key]?.children.isNullOrEmpty()) {
          // this field has no nested elements and/or is selectively disclosable only as a whole:
          digestSDClaim(entry.key, entry.value, digests2disclosures)
        } else {
          // the nested properties could be selectively disclosable individually
          // recursively generate SD payload for nested object:
          val nestedSDPayload = generateSDPayload(entry.value.jsonObject, sdMap[entry.key]!!.children!!, digests2disclosures)
          if(sdMap[entry.key]?.sd == true) {
            // this nested object is also selectively disclosable as a whole
            // so let's compute the digest and disclosure for the nested SD payload:
            digestSDClaim(entry.key, nestedSDPayload, digests2disclosures)
          } else {
            // this nested object is not selectively disclosable as a whole, add the nested SD payload as it is:
            sdPayload[entry.key] = nestedSDPayload
            // no digest/disclosure is added for this field (though the nested properties may have generated digests and disclosures)
            null
          }
        }
      }.filterNotNull().toSet()

      if(digests.isNotEmpty()) {
        sdPayload.put(SDJwt.DIGESTS_KEY, buildJsonArray {
          digests.forEach { add(it) }
          if(sdMap.decoyMode != DecoyMode.NONE && sdMap.decoys > 0) {
            val numDecoys = when(sdMap.decoyMode) {
              // NOTE: SecureRandom.nextInt always returns 0! Use nextDouble instead
              DecoyMode.RANDOM -> SecureRandom.nextDouble(1.0, sdMap.decoys+1.0).toInt()
              DecoyMode.FIXED -> sdMap.decoys
              else -> 0
            }
            repeat(numDecoys) {
              add(digest(SecureRandom.nextBytes(32).base64Url))
            }
          }
        })
      }
      return JsonObject(sdPayload)
    }

    fun createSDPayload(fullPayload: JsonObject, disclosureMap: SDMap): SDPayload {
      val digestedDisclosures = mutableMapOf<String, SDisclosure>()
      return SDPayload(
        undisclosedPayload = generateSDPayload(fullPayload, disclosureMap, digestedDisclosures),
        digestedDisclosures = digestedDisclosures
      )
    }

    fun createSDPayload(jwtClaimsSet: JWTClaimsSet, disclosureMap: SDMap)
      = createSDPayload(Json.parseToJsonElement(jwtClaimsSet.toString()).jsonObject, disclosureMap)

    fun createSDPayload(fullPayload: JsonObject, undisclosedPayload: JsonObject, decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0)
      = createSDPayload(fullPayload, SDMap.generateSDMap(fullPayload, undisclosedPayload, decoyMode, decoys))

    fun createSDPayload(fullJWTClaimsSet: JWTClaimsSet, undisclosedJWTClaimsSet: JWTClaimsSet, decoyMode: DecoyMode = DecoyMode.NONE, decoys: Int = 0)
      = createSDPayload(
        Json.parseToJsonElement(fullJWTClaimsSet.toString()).jsonObject,
        Json.parseToJsonElement(undisclosedJWTClaimsSet.toString()).jsonObject,
        decoyMode, decoys
      )

    fun createFrom(undisclosedPayload: JsonObject, disclosures: Set<String>): SDPayload {
      return SDPayload(
        undisclosedPayload,
        disclosures.associate { Pair(digest(it), SDisclosure.parse(it)) })
    }
  }
}