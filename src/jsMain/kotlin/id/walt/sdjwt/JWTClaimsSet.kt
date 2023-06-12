package id.walt.sdjwt

@JsModule("jose")
external class JWTPayload {
  val sub: String
  val aud: String
  val exp: Number
  val iat: Number
  val iss: String
  val nbf: Number
  val jti: String
}

actual typealias JWTClaimsSet = JWTPayload