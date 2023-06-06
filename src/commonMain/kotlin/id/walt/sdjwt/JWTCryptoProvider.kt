package id.walt.sdjwt

import kotlinx.serialization.json.JsonObject

/**
 * Crypto provider, that provides signing and verifying of standard JWTs on the target platform
 * Can be implemented by library user, to integrate their own or custom JWT crypto library
 * Default implementations exist for some platforms.
 * @see SimpleJWTCryptoProvider
 */
interface JWTCryptoProvider {
  /**
   * Interface method to create a signed JWT for the given JSON payload object, with and optional keyID.
   * @param payload The JSON payload of the JWT to be signed
   * @param keyID Optional keyID of the signing key to be used, if required by crypto provider
   */
  fun sign(payload: JsonObject, keyID: String? = null): String

  /**
   * Interface method for verifying a JWT signature
   * @param jwt A signed JWT token to be verified
   */
  fun verify(jwt: String): Boolean
}

/**
 * Expected default implementation for JWTCryptoProvider on each platform
 * Implemented in platform specific modules
 * @see JWTCryptoProvider
 */
expect class SimpleJWTCryptoProvider : JWTCryptoProvider