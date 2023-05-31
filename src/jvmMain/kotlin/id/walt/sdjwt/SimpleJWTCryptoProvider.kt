package id.walt.sdjwt

import com.nimbusds.jose.*
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.JsonObject

actual class SimpleJWTCryptoProvider(
  val jwsAlgorithm: JWSAlgorithm,
  private val jwsSigner: JWSSigner?,
  private val jwsVerifier: JWSVerifier?
) : JWTCryptoProvider {

  override fun sign(payload: JsonObject, keyID: String?): String {
    if(jwsSigner == null) {
      throw Exception("No signer available")
    }
    return SignedJWT(
      JWSHeader.Builder(jwsAlgorithm).type(JOSEObjectType.JWT).keyID(keyID).build(),
      JWTClaimsSet.parse(payload.toString())
    ).also {
      it.sign(jwsSigner)
    }.serialize()
  }

  override fun verify(jwt: String): Boolean {
    if(jwsVerifier == null) {
      throw Exception("No verifier available")
    }
    return SignedJWT.parse(jwt).verify(jwsVerifier)
  }
}

