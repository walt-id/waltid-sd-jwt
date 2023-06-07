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
  // Generate shared secret for HMAC crypto algorithm
  private val sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"

  @Test
  fun testSignJwt() {

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

  @Test
  fun presentSDJwt() {
    // parse previously created SD-JWT
    val sdJwt = SDJwt.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~WyJ4RFk5VjBtOG43am82ZURIUGtNZ1J3Iiwic3ViIiwiMTIzIl0")

    // present without disclosing SD fields
    val presentedUndisclosedJwt = sdJwt.present(discloseAll = false)
    println(presentedUndisclosedJwt)

    // present disclosing all SD fields
    val presentedDisclosedJwt = sdJwt.present(discloseAll = true)
    println(presentedDisclosedJwt)

    // present disclosing selective fields, using SDMap
    val presentedSelectiveJwt = sdJwt.present(mapOf(
      "sub" to SDField(true)
    ).toSDMap())
    println(presentedSelectiveJwt)

    // present disclosing fields, using JSON paths
    val presentedSelectiveJwt2 = sdJwt.present(SDMap.generateSDMap(listOf("sub")))
    println(presentedSelectiveJwt2)

  }

  @Test
  fun parseAndVerify() {
    //val presentedUndisclosedJwt
  }
}