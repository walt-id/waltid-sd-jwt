package id.walt.sdjwt

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.JsonObject


actual class JWTFactory(private val algorithm: JWSAlgorithm, private val keyId: String, private val jwsSigner: JWSSigner) {
  actual fun sign(
    payload: JsonObject
  ): String {
    return SignedJWT(
      JWSHeader.Builder(algorithm)
        .keyID(keyId)
        .type(JOSEObjectType.JWT)
        .build(),
      JWTClaimsSet.parse(payload.toString())).also {
        it.sign(jwsSigner)
      }.serialize()
  }
}