package id.walt.sdjwt

import com.nimbusds.jose.*
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.JsonObject

actual abstract class SimpleJWTCryptoProvider : JWTCryptoProvider {
  abstract val jwsSigner: JWSSigner
  abstract val jwsVerifier: JWSVerifier
  abstract val jwsAlgorithm: JWSAlgorithm

  override fun sign(payload: JsonObject, keyID: String?): String {
    return SignedJWT(
      JWSHeader.Builder(jwsAlgorithm).type(JOSEObjectType.JWT).keyID(keyID).build(),
      JWTClaimsSet.parse(payload.toString())
    ).also {
      it.sign(jwsSigner)
    }.serialize()
  }

  override fun verify(jwt: String): Boolean {
    return SignedJWT.parse(jwt).verify(jwsVerifier)
  }
}

class HmacJWTCryptoProvider(
  override val jwsAlgorithm: JWSAlgorithm,
  sharedSecret: ByteArray
): SimpleJWTCryptoProvider() {

  override val jwsSigner: JWSSigner = MACSigner(sharedSecret)
  override val jwsVerifier: JWSVerifier = MACVerifier(sharedSecret)

  init {
    when(jwsAlgorithm) {
      JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512 -> {}
      else -> throw Exception("Unsupported algorithm for HMAC crypto provider")
    }
  }
}

