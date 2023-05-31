package id.walt.sdjwt

import io.kotest.assertions.json.shouldMatchJson
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.*
import kotlin.test.Test

class SDJwtTest {
  @Test
  fun testParseSdJwt() {
    val sdJwtString = "eyJraWQiOiJkaWQ6a2V5Ono2TWtuM2dWOFY2M2JScEJNdEFwbm5BaWhDTXZEYVBlcno2aWFyMURwZE5LZTNrMSN6Nk1rbjNnVjhWNjNiUnBCTXRBcG5uQWloQ012RGFQZXJ6NmlhcjFEcGROS2UzazEiLCJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSJ9.eyJzdWIiOiJkaWQ6a2V5Ono2TWtuM2dWOFY2M2JScEJNdEFwbm5BaWhDTXZEYVBlcno2aWFyMURwZE5LZTNrMSIsIm5iZiI6MTY4NDkxOTg2NCwiaXNzIjoiZGlkOmtleTp6Nk1rbjNnVjhWNjNiUnBCTXRBcG5uQWloQ012RGFQZXJ6NmlhcjFEcGROS2UzazEiLCJpYXQiOjE2ODQ5MTk4NjQsInZjIjp7InR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJWZXJpZmlhYmxlSWQiXSwiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiXSwiaWQiOiJ1cm46dXVpZDplMDNjMDY2OC0yMDhmLTRkNzctYTBjNi02ZDBkZjAxYWRmYWQiLCJpc3N1ZXIiOiJkaWQ6a2V5Ono2TWtuM2dWOFY2M2JScEJNdEFwbm5BaWhDTXZEYVBlcno2aWFyMURwZE5LZTNrMSIsImlzc3VhbmNlRGF0ZSI6IjIwMjMtMDUtMjRUMDk6MTc6NDRaIiwiaXNzdWVkIjoiMjAyMy0wNS0yNFQwOToxNzo0NFoiLCJ2YWxpZEZyb20iOiIyMDIzLTA1LTI0VDA5OjE3OjQ0WiIsImNyZWRlbnRpYWxTY2hlbWEiOnsiaWQiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vd2FsdC1pZC93YWx0aWQtc3Npa2l0LXZjbGliL21hc3Rlci9zcmMvdGVzdC9yZXNvdXJjZXMvc2NoZW1hcy9WZXJpZmlhYmxlSWQuanNvbiIsInR5cGUiOiJGdWxsSnNvblNjaGVtYVZhbGlkYXRvcjIwMjEifSwiZXZpZGVuY2UiOlt7ImRvY3VtZW50UHJlc2VuY2UiOlsiUGh5c2ljYWwiXSwiZXZpZGVuY2VEb2N1bWVudCI6WyJQYXNzcG9ydCJdLCJzdWJqZWN0UHJlc2VuY2UiOiJQaHlzaWNhbCIsInR5cGUiOlsiRG9jdW1lbnRWZXJpZmljYXRpb24iXSwidmVyaWZpZXIiOiJkaWQ6ZWJzaToyQTlCWjlTVWU2QmF0YWNTcHZzMVY1Q2RqSHZMcFE3YkVzaTJKYjZMZEhLblF4YU4ifV0sIl9zZCI6WyJzRkoxY1BOR2d5NktrRzAxOW9tdFBvVm5GYXR6clRXQkV0Si1yQmpzQU1VIl19LCJqdGkiOiJ1cm46dXVpZDplMDNjMDY2OC0yMDhmLTRkNzctYTBjNi02ZDBkZjAxYWRmYWQifQ.XqhNYYB9CITa0KCMOj1v1tbQvj3sfuDmGaKL3sDDJndQPGEa_QosbunSnBef5O4optTslUOSaplR7BiTiY2cCQ~WyJfZ2RWc3dIS2U2b3ZVMElYT3NXQ1Z3IiwiZGF0ZU9mQmlydGgiLCIxOTgzLTA3LTA1Il0~WyJsYVpHcU5rczE1YU5USEFvYnFfWEx3IiwiZmlyc3ROYW1lIiwiU2V2ZXJpbiJd~WyJDc1FfVkFaeUVzVmxSWWQ1YjJPNWhRIiwiY3JlZGVudGlhbFN1YmplY3QiLHsiaWQiOiJkaWQ6a2V5Ono2TWtuM2dWOFY2M2JScEJNdEFwbm5BaWhDTXZEYVBlcno2aWFyMURwZE5LZTNrMSIsImN1cnJlbnRBZGRyZXNzIjpbIlZpZW5uYSJdLCJmYW1pbHlOYW1lIjoiU3RhbXBsZXIiLCJnZW5kZXIiOiJtYWxlIiwibmFtZUFuZEZhbWlseU5hbWVBdEJpcnRoIjoiSmFuZSBET0UiLCJwZXJzb25hbElkZW50aWZpZXIiOiIwOTA0MDA4MDg0SCIsInBsYWNlT2ZCaXJ0aCI6IkdyYXoiLCJfc2QiOlsiUXh4ZXlNbHk3dU9feWNRaThuU2Zmb2VUb2JQSDRVZ0JfVGh3LUMyMTJDNCIsImNJeXVGWVNBUjAtZ2JpMGxQbmZTWVQySExEdlpsTlBOSGoxUGY0am9uek0iXX1d"
    val sdJwt = SDJwt.parse(sdJwtString)
    sdJwt.sdPayload.undisclosedPayload shouldContainKey "sub"
    sdJwt.sdPayload.undisclosedPayload shouldContainKey "vc"
    sdJwt.sdPayload.undisclosedPayload["vc"]!!.jsonObject shouldContainKey SDJwt.DIGESTS_KEY
    sdJwt.sdPayload.undisclosedPayload["vc"]!!.jsonObject shouldNotContainKey "credentialSubject"

    sdJwt.sdPayload.fullPayload["vc"]!!.jsonObject shouldContainKey "credentialSubject"

    println(sdJwt.sdPayload.fullPayload.toString())
  }

  @Test
  fun testSDPayloadGeneration() {
    val fullPayload = buildJsonObject {
      put("sub", "1234")
      put("nestedObject", buildJsonObject {
        put("arrProp", buildJsonArray {
          add("item 1")
          add("item 2")
        })
      })
    }

    val sdPayload_1 = SDPayload.createSDPayload(fullPayload, buildJsonObject {  })

    sdPayload_1.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdPayload_1.undisclosedPayload.keys shouldNotContainAnyOf setOf("sub", "nestedObject")
    sdPayload_1.fullPayload.toString() shouldMatchJson fullPayload.toString()


    val sdPayload_2 = SDPayload.createSDPayload(fullPayload, buildJsonObject {
      put("nestedObject", buildJsonObject {  })
    })

    sdPayload_2.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdPayload_2.undisclosedPayload shouldContainKey "nestedObject"
    sdPayload_2.undisclosedPayload shouldNotContainKey "sub"
    sdPayload_2.undisclosedPayload["nestedObject"]!!.jsonObject shouldContainKey SDJwt.DIGESTS_KEY
    sdPayload_2.undisclosedPayload["nestedObject"]!!.jsonObject shouldNotContainKey "arrProp"
    sdPayload_2.fullPayload.toString() shouldMatchJson fullPayload.toString()


    val sdPayload_3 = SDPayload.createSDPayload(fullPayload, mapOf(
      "sub" to SDField(true),
      "nestedObject" to SDField(true, mapOf(
        "arrProp" to SDField(true)
      ))
    ))

    sdPayload_3.undisclosedPayload shouldContainKey SDJwt.DIGESTS_KEY
    sdPayload_3.undisclosedPayload.keys shouldNotContainAnyOf setOf("sub", "nestedObject")
    val nestedDisclosure = sdPayload_3.sDisclosures.firstOrNull() { sd -> sd.key == "nestedObject" && sd.value is JsonObject }
    nestedDisclosure shouldNotBe null
    nestedDisclosure!!.value.jsonObject shouldContainKey SDJwt.DIGESTS_KEY
    nestedDisclosure.value.jsonObject shouldNotContainKey "arrProp"
    sdPayload_3.fullPayload.toString() shouldMatchJson fullPayload.toString()
  }
}