package id.walt.sdjwt

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.serialization.json.JsonObject
import kotlin.js.Promise

@ExperimentalJsExport
@JsExport
class SDJwtAsync(
  sdJwt: SDJwt
): SDJwt(sdJwt.jwt, sdJwt.header, sdJwt.sdPayload, sdJwt.holderJwt, sdJwt.isPresentation) {

  @JsName("verifyAsync")
  fun verifyAsyncJs(jwtCryptoProvider: AsyncJWTCryptoProvider): Promise<Boolean> = GlobalScope.promise {
    verifyAsync(jwtCryptoProvider)
  }

  companion object {
    fun verifyAndParseAsync(sdJwt: String, jwtCryptoProvider: AsyncJWTCryptoProvider): Promise<SDJwtAsync> = GlobalScope.promise {
      SDJwtAsync(
        SDJwt.verifyAndParseAsync(sdJwt, jwtCryptoProvider)
      )
    }

    fun signAsync(sdPayload: SDPayload, jwtCryptoProvider: AsyncJWTCryptoProvider, keyID: String? = null, withHolderJwt: String? = null): Promise<SDJwt> = GlobalScope.promise {
      SDJwtAsync(
        SDJwt.signAsync(sdPayload, jwtCryptoProvider)
      )
    }
  }
}