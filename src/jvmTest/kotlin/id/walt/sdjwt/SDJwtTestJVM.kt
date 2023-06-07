package id.walt.sdjwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
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
    // Generate shared secret for HMAC crypto algorithm
    val sharedSecret =  korlibs.crypto.SecureRandom.nextBytes(32)

    // Create SimpleJWTCryptoProvider with MACSigner and MACVerifier
    val cryptoProvider = SimpleJWTCryptoProvider(JWSAlgorithm.HS256, MACSigner(sharedSecret), MACVerifier(sharedSecret))

    // Create original JWT claims set, using nimbusds claims set builder
    val originalClaimsSet = JWTClaimsSet.Builder()
      .subject("123")
      .audience("456")
    .build()

    // Create undisclosed claims set, by removing e.g. subject property from original claims set
    val undisclosedClaimsSet = JWTClaimsSet.Builder(originalClaimsSet)
      .subject(null)
      .build()

    // Create SD payload by comparing original claims set with undisclosed claims set
    val sdPayload = SDPayload.createSDPayload(originalClaimsSet, undisclosedClaimsSet)

    // Create and sign SD-JWT using the generated SD payload and the previously configured crypto provider
    val sdJwt = SDJwt.sign(sdPayload, cryptoProvider)
    // Print SD-JWT
    println(sdJwt)

    sdJwt.sdPayload.undisclosedPayload shouldNotContainKey "sub"
    sdJwt.sdPayload.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdJwt.sdPayload.undisclosedPayload shouldContainKey "aud"
    sdJwt.disclosures shouldHaveSize 1
    sdJwt.sdPayload.digestedDisclosures[sdJwt.sdPayload.undisclosedPayload[SDJwt.DIGESTS_KEY]!!.jsonArray[0].jsonPrimitive.content]!!.key shouldBe "sub"
    sdJwt.sdPayload.fullPayload.toString() shouldMatchJson originalClaimsSet.toString()

    sdJwt.verify(cryptoProvider) shouldBe true
  }
}