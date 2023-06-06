package id.walt.sdjwt

import korlibs.crypto.encoding.Base64
import kotlinx.serialization.json.*

/**
 * SD-JWT object, providing signed JWT token, header and payload with disclosures, as well as optional holder binding
 */
class SDJwt (
  val jwt: String,
  val header: JsonObject,
  val sdPayload: SDPayload,
  val holderJwt: String? = null
) {
  /**
   * Encoded disclosures, included in this SD-JWT
   */
  val disclosures
    get() = sdPayload.sDisclosures.map { it.disclosure }.toSet()

  /**
   * Signature algorithm from JWT header
   */
  val algorithm
    get() = header["alg"]?.jsonPrimitive?.contentOrNull

  /**
   * Signature key ID from JWT header, if present
   */
  val keyID
    get() = header["kid"]?.jsonPrimitive?.contentOrNull

  /**
   * Signature key in JWK format, from JWT header, if present
   */
  val jwk
    get() = header["jwk"]?.jsonPrimitive?.contentOrNull

  override fun toString() = toString(false)

  fun toString(formatForPresentation: Boolean): String {
    return listOf(jwt)
      .plus(disclosures)
      .plus(holderJwt?.let { listOf(it) } ?: (if(formatForPresentation) listOf("") else listOf()))
      .joinToString(SEPARATOR_STR)
  }

  /**
   * Present SD-JWT with selection of disclosures
   * @param sdMap Selective disclosure map, indicating for each field (recursively) whether it should be disclosed or undisclosed in the presentation
   * @param withHolderJwt Optionally, adds the provided JWT as holder binding to the presented SD-JWT token
   */
  fun present(sdMap: Map<String, SDField>?, withHolderJwt: String? = null): SDJwt {
    return SDJwt(
      jwt,
      header,
      sdMap?.let { sdPayload.withSelectiveDisclosures(it) } ?: sdPayload.withoutDisclosures(),
      withHolderJwt ?: holderJwt)
  }

  /**
   * Shortcut to presenting the SD-JWT, with all disclosures selected or unselected
   * @param discloseAll true: disclose all selective disclosures, false: all selective disclosures remain undisclosed
   * @param withHolderJwt Optionally, adds the provided JWT as holder binding to the presented SD-JWT token
   */
  fun present(discloseAll: Boolean, withHolderJwt: String? = null): SDJwt {
    return SDJwt(
      jwt,
      header,
      if(discloseAll) {
        sdPayload
      } else {
        sdPayload.withoutDisclosures()
      },
      withHolderJwt ?: holderJwt
    )
  }

  /**
   * Verify the SD-JWT by checking the signature, using the given JWT crypto provider, and matching the disclosures against the digests in the JWT payload
   * @param jwtCryptoProvider JWT crypto provider, that implements standard JWT token verification on the target platform
   */
  fun verify(jwtCryptoProvider: JWTCryptoProvider): Boolean {
    return sdPayload.verifyDisclosures() && jwtCryptoProvider.verify(jwt)
  }

  companion object {
    const val DIGESTS_KEY = "_sd"
    const val SEPARATOR = '~'
    const val SEPARATOR_STR = SEPARATOR.toString()
    const val SD_JWT_PATTERN = "^(?<sdjwt>(?<header>[A-Za-z0-9-_]+)\\.(?<body>[A-Za-z0-9-_]+)\\.(?<signature>[A-Za-z0-9-_]+))(?<disclosures>(~([A-Za-z0-9-_]+))+)?(~(?<holderjwt>([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))?)?\$"

    /**
     * Parse SD-JWT from a token string
     */
    fun parse(sdJwt: String): SDJwt {
      val matchResult = Regex(SD_JWT_PATTERN).matchEntire(sdJwt) ?: throw Exception("Invalid SD-JWT format")
      val matchedGroups = matchResult.groups as MatchNamedGroupCollection
      val disclosures = matchedGroups["disclosures"]?.value?.trim(SEPARATOR)?.split(SEPARATOR)?.toSet() ?: setOf()
      return SDJwt(
        matchedGroups["sdjwt"]!!.value,
        Json.parseToJsonElement(Base64.decode(matchedGroups["header"]!!.value, true).decodeToString()).jsonObject,
        SDPayload.parse(
          matchedGroups["body"]!!.value,
          disclosures),
        matchedGroups["holderjwt"]?.value
      )
    }

    /**
     * Parse SD-JWT from a token string and verify it
     * @return parsed SD-JWT, if token has been verified
     * @throws Exception if SD-JWT cannot be verified
     */
    fun verifyAndParse(sdJwt: String, jwtCryptoProvider: JWTCryptoProvider): SDJwt {
      return parse(sdJwt).also {
        if(!it.verify(jwtCryptoProvider)) {
          throw Exception("SD-JWT could not be verified")
        }
      }
    }

    /**
     * Sign the given payload as SD-JWT token, using the given JWT crypto provider, optionally with the specified key ID and holder binding
     * @param sdPayload Payload with selective disclosures to be signed
     * @param jwtCryptoProvider Crypto provider implementation, that supports JWT creation on the target platform
     * @param keyID Optional key ID, if the crypto provider implementation requires it
     * @param withHolderJwt Optionally, append the given holder binding JWT to the signed SD-JWT token
     * @return  The signed SDJwt object
     */
    fun sign(sdPayload: SDPayload, jwtCryptoProvider: JWTCryptoProvider, keyID: String? = null, withHolderJwt: String? = null): SDJwt {
      val sdJwt = parse(jwtCryptoProvider.sign(sdPayload.undisclosedPayload, keyID))
     return SDJwt(
        jwt = sdJwt.jwt,
        header = sdJwt.header,
        sdPayload = sdPayload,
        holderJwt = withHolderJwt
     )
    }

    /**
     * Check the given string, whether it matches the pattern of an SD-JWT
     */
    fun isSDJwt(value: String): Boolean {
      return Regex(SD_JWT_PATTERN).matches(value)
    }
  }
}
