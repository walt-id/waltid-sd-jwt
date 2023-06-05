package id.walt.sdjwt

import korlibs.crypto.encoding.Base64
import kotlinx.serialization.json.*

class SDJwt (
  val jwt: String,
  val header: JsonObject,
  val sdPayload: SDPayload,
  val holderJwt: String? = null
) {
  val disclosures
    get() = sdPayload.sDisclosures.map { it.disclosure }.toSet()

  val algorithm
    get() = header["alg"]?.jsonPrimitive?.contentOrNull

  val keyID
    get() = header["kid"]?.jsonPrimitive?.contentOrNull

  val jwk
    get() = header["jwk"]?.jsonPrimitive?.contentOrNull

  override fun toString() = toString(false)

  fun toString(formatForPresentation: Boolean): String {
    return listOf(jwt)
      .plus(disclosures)
      .plus(holderJwt?.let { listOf(it) } ?: (if(formatForPresentation) listOf("") else listOf()))
      .joinToString(SEPARATOR_STR)
  }

  fun present(sdMap: Map<String, SDField>?, withHolderJwt: String? = null): SDJwt {
    return SDJwt(
      jwt,
      header,
      sdMap?.let { sdPayload.withSelectiveDisclosures(it) } ?: sdPayload.withoutDisclosures(),
      withHolderJwt ?: holderJwt)
  }

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

  fun verify(jwtCryptoProvider: JWTCryptoProvider): Boolean {
    return sdPayload.verifyDisclosures() && jwtCryptoProvider.verify(jwt)
  }

  companion object {
    const val DIGESTS_KEY = "_sd"
    const val SEPARATOR = '~'
    const val SEPARATOR_STR = SEPARATOR.toString()
    const val SD_JWT_PATTERN = "^(?<sdjwt>(?<header>[A-Za-z0-9-_]+)\\.(?<body>[A-Za-z0-9-_]+)\\.(?<signature>[A-Za-z0-9-_]+))(?<disclosures>(~([A-Za-z0-9-_]+))+)?(~(?<holderjwt>([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))?)?\$"

    fun parse(sdJwt: String): SDJwt {
      val matchResult = Regex(SD_JWT_PATTERN).matchEntire(sdJwt) ?: throw Exception("Invalid SD-JWT format")
      val matchedGroups = matchResult.groups as MatchNamedGroupCollection
      val disclosures = matchedGroups["disclosures"]?.value
        ?.trim(SEPARATOR)
        ?.split(SEPARATOR)
        ?.toSet() ?: setOf()
      return SDJwt(
        matchedGroups["sdjwt"]!!.value,
        Json.parseToJsonElement(Base64.decode(matchedGroups["header"]!!.value, true).decodeToString()).jsonObject,
        SDPayload.createFrom(
          sdJwt.split(".")[1].let { Json.parseToJsonElement(Base64.decode(it, url = true).decodeToString()).jsonObject },
          disclosures),
        matchedGroups["holderjwt"]?.value
      )
    }

    fun verifyAndParse(sdJwt: String, jwtCryptoProvider: JWTCryptoProvider): SDJwt {
      return parse(sdJwt).also {
        if(!it.verify(jwtCryptoProvider)) {
          throw Exception("SD-JWT could not be verified")
        }
      }
    }

    fun sign(sdPayload: SDPayload, jwtCryptoProvider: JWTCryptoProvider, keyID: String? = null, withHolderJwt: String? = null): SDJwt {
      val sdJwt = parse(jwtCryptoProvider.sign(sdPayload.undisclosedPayload, keyID))
     return SDJwt(
        jwt = sdJwt.jwt,
        header = sdJwt.header,
        sdPayload = sdPayload,
        holderJwt = withHolderJwt
     )
    }

    fun isSDJwt(value: String): Boolean {
      return Regex(SD_JWT_PATTERN).matches(value)
    }
  }
}
