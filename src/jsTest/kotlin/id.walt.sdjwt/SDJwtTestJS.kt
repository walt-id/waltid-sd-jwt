package id.walt.sdjwt

import io.kotest.assertions.json.shouldMatchJson
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.khronos.webgl.Uint8Array
import kotlin.test.Test

class SDJwtTestJS {

  // Generate shared secret for HMAC crypto algorithm
  private val sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"
  @OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class)
  @Test
  fun test1() = GlobalScope.promise {
    val cryptoProvider = SimpleAsyncJWTCryptoProvider("HS256",
      Uint8Array(sharedSecret.encodeToByteArray().toTypedArray()), null)
    // Create original JWT claims set, using nimbusds claims set builder
    val originalClaimsSet = buildJsonObject {
      put("sub", "123")
      put("aud", "456")
    }

    // Create undisclosed claims set, by removing e.g. subject property from original claims set
    val undisclosedClaimsSet = buildJsonObject {
      put("aud", "456")
    }

    // Create SD payload by comparing original claims set with undisclosed claims set
    val sdPayload = SDPayload.createSDPayload(originalClaimsSet, undisclosedClaimsSet)

    // Create and sign SD-JWT using the generated SD payload and the previously configured crypto provider
    val sdJwt = SDJwt.signAsync(sdPayload, cryptoProvider)
    // Print SD-JWT
    println(sdJwt)

    sdJwt.sdPayload.undisclosedPayload shouldNotContainKey "sub"
    sdJwt.sdPayload.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdJwt.sdPayload.undisclosedPayload shouldContainKey "aud"
    sdJwt.disclosures shouldHaveSize 1
    sdJwt.sdPayload.digestedDisclosures[sdJwt.sdPayload.undisclosedPayload[SDJwt.DIGESTS_KEY]!!.jsonArray[0].jsonPrimitive.content]!!.key shouldBe "sub"
    sdJwt.sdPayload.fullPayload.toString() shouldMatchJson originalClaimsSet.toString()

    sdJwt.verifyAsync(cryptoProvider) shouldBe true
    SDJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiI0NTciLCJfc2QiOlsibTcyQ0tyVHhYckhlWUJSQTFBVFQ1S0t4NGdFWExlOVhqVFROakdRWkVQNCJdfQ.Tltz2SGxmdIpD_ny1XSTn89rQSmYsl9EcsXxsfJE0wo", buildJsonObject {  }, sdJwt.sdPayload)
      .verifyAsync(cryptoProvider) shouldBe false
  }
}