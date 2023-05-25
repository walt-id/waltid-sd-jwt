package id.walt.sdjwt

import korlibs.crypto.encoding.Base64
import kotlinx.serialization.json.*

class SDJwt (
  val jwt: String,
  val sdPayload: SDPayload,
  val holderJwt: String? = null
) {
  val disclosures
    get() = sdPayload.sDisclosures.map { it.disclosure }.toSet()

  override fun toString() = toString(false)

  fun toString(formatForPresentation: Boolean): String {
    return listOf(jwt)
      .plus(disclosures)
      .plus(holderJwt?.let { listOf(it) } ?: (if(formatForPresentation) listOf("") else listOf()))
      .joinToString(SEPARATOR_STR)
  }

  companion object {
    const val DIGESTS_KEY = "_sd"
    const val SEPARATOR = '~'
    const val SEPARATOR_STR = SEPARATOR.toString()
    const val SD_JWT_PATTERN = "^(?<sdjwt>([A-Za-z0-9-_]+)\\.(?<body>[A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))(?<disclosures>(~([A-Za-z0-9-_]+))+)?(~(?<holderjwt>([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))?)?\$"

    fun parse(sdJwt: String): SDJwt {
      val matchResult = Regex(SDJwt.SD_JWT_PATTERN).matchEntire(sdJwt) ?: throw Exception("Invalid SD-JWT format")
      val matchedGroups = matchResult.groups as MatchNamedGroupCollection
      val disclosures = matchedGroups["disclosures"]?.value
        ?.trim(SDJwt.SEPARATOR)
        ?.split(SDJwt.SEPARATOR)
        ?.toSet() ?: setOf()
      return SDJwt(
        matchedGroups["sdjwt"]!!.value,
        SDPayload.createFrom(
          sdJwt.split(".")[1].let { Json.parseToJsonElement(Base64.decode(it, url = true).decodeToString()).jsonObject },
          disclosures),
        matchedGroups["holderjwt"]?.value
      )
    }

    fun createFrom(sdPayload: SDPayload, jwtFactory: JWTFactory, holderJwt: String? = null): SDJwt {
     return SDJwt(
        jwt = jwtFactory.sign(sdPayload.undisclosedPayload),
        sdPayload = sdPayload,
        holderJwt = holderJwt
     )
    }
  }
}
