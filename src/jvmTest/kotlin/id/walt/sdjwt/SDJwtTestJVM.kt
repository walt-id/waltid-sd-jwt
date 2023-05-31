package id.walt.sdjwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import io.kotest.assertions.json.shouldMatchJson
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class SDJwtTestJVM {
  @Test
  fun testSignJwt() {
    val originalClaimsSet = JWTClaimsSet.Builder().subject("123").audience("456").build()
    val sdPayload = SDPayload.createSDPayload(originalClaimsSet, JWTClaimsSet.Builder(originalClaimsSet).subject(null).build())

    val cryptoProvider = HmacJWTCryptoProvider(JWSAlgorithm.HS256, korlibs.crypto.SecureRandom.nextBytes(32))
    val sdJwt = SDJwt.sign(sdPayload, cryptoProvider)

    sdJwt.sdPayload.undisclosedPayload shouldNotContainKey "sub"
    sdJwt.sdPayload.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdJwt.sdPayload.undisclosedPayload shouldContainKey "aud"
    sdJwt.disclosures shouldHaveSize 1
    sdJwt.sdPayload.digestedDisclosures[sdJwt.sdPayload.undisclosedPayload[SDJwt.DIGESTS_KEY]!!.jsonArray[0].jsonPrimitive.content]!!.key shouldBe "sub"
    sdJwt.sdPayload.fullPayload.toString() shouldMatchJson originalClaimsSet.toString()

    sdJwt.verify(cryptoProvider) shouldBe true
  }
}