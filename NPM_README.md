<div align="center">
 <h1>SD-JWT library</h1>
 <span>by </span><a href="https://walt.id">walt.id</a>
  <p>Create JSON Web Tokens (JWTs) that support <b>Selective Disclosure</b><p>

<a href="https://walt.id/community">
<img src="https://img.shields.io/badge/Join-The Community-blue.svg?style=flat" alt="Join community!" />
</a>
<a href="https://twitter.com/intent/follow?screen_name=walt_id">
<img src="https://img.shields.io/twitter/follow/walt_id.svg?label=Follow%20@walt_id" alt="Follow @walt_id" />
</a>

</div>

## What is the SD-JWT library?

This libary implements the **Selective Disclosure JWT (SD-JWT)**
specification:  [draft-ietf-oauth-selective-disclosure-jwt-04](https://datatracker.ietf.org/doc/draft-ietf-oauth-selective-disclosure-jwt/04/).

### Features

* **Create and sign** SD-JWT tokens
    * Choose selectively disclosable payload fields (SD fields)
    * Create digests for SD fields and insert into JWT body payload
    * Create and append encoded disclosure strings for SD fields to JWT token
    * Add random or fixed number of **decoy digests** on each nested object property
* **Present** SD-JWT tokens
    * Selection of fields to be disclosed
    * Support for appending optional holder binding
* Full support for **nested SD fields** and **recursive disclosures**
* **Parse** SD-JWT tokens and restore original payload with disclosed fields
* **Verify** SD-JWT token
    * Signature verification
    * Hash comparison and tamper check of the appended disclosures
* Support for **integration** with various crypto libraries and frameworks, to perform the cryptographic operations and key management

### Further information

Checkout the [documentation regarding SD-JWTs](https://docs.walt.id/v/ssikit/concepts/selective-disclosure), to find out more.

## Examples

**Build payload, sign and present examples**

```javascript
import sdlib from "waltid-sd-jwt"

const sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"
const cryptoProvider = new sdlib.id.walt.sdjwt.SimpleAsyncJWTCryptoProvider("HS256", new TextEncoder().encode(sharedSecret))

const sdMap = new sdlib.id.walt.sdjwt.SDMapBuilder(sdlib.id.walt.sdjwt.DecoyMode.FIXED.name, 2).addField("sub", true,
    new sdlib.id.walt.sdjwt.SDMapBuilder().addField("child", true).build()
).build()

console.log(sdMap, JSON.stringify(sdMap))

const sdPayload = new sdlib.id.walt.sdjwt.SDPayloadBuilder({"sub": "123", "aud": "345"}).buildForUndisclosedPayload({"aud": "345"})
const sdPayload2 = new sdlib.id.walt.sdjwt.SDPayloadBuilder({"sub": "123", "aud": "345"}).buildForSDMap(sdMap)

const jwt = await sdlib.id.walt.sdjwt.SDJwtJS.Companion.signAsync(
    sdPayload, cryptoProvider)
console.log(jwt.toString())

const jwt2 = await sdlib.id.walt.sdjwt.SDJwtJS.Companion.signAsync(
    sdPayload2, cryptoProvider)
console.log(jwt2.toString())

console.log("Verified:", (await jwt.verifyAsync(cryptoProvider)).verified)
console.log("Verified:", (await jwt2.verifyAsync(cryptoProvider)).verified)

const presentedJwt = await jwt.presentAllAsync(false)
console.log("Presented undisclosed SD-JWT:", presentedJwt.toString())
console.log("Verified: ", (await presentedJwt.verifyAsync(cryptoProvider)).verified)

const sdMap2 = new sdlib.id.walt.sdjwt.SDMapBuilder().buildFromJsonPaths(["sub"])
console.log("SDMap2:", sdMap2)
const presentedJwt2 = await jwt.presentAsync(sdMap2)
console.log("Presented disclosed SD-JWT:", presentedJwt2.toString())
const verificationResultPresentedJwt2 = await presentedJwt2.verifyAsync(cryptoProvider)
console.log("Presented payload", verificationResultPresentedJwt2.sdJwt.fullPayload)
console.log("Presented disclosures", verificationResultPresentedJwt2.sdJwt.disclosureObjects)
console.log("Presented disclosure strings", verificationResultPresentedJwt2.sdJwt.disclosures)
console.log("Verified: ", verificationResultPresentedJwt2.verified)
console.log("SDMap reconstructed", presentedJwt2.sdMap)

```

## Join the community

* Connect and get the latest
  updates: [Discord](https://discord.gg/AW8AgqJthZ) | [Newsletter](https://walt.id/newsletter) | [YouTube](https://www.youtube.com/channel/UCXfOzrv3PIvmur_CmwwmdLA) | [Twitter](https://mobile.twitter.com/walt_id)
* Get help, request features and report bugs: [GitHub Discussions](https://github.com/walt-id/.github/discussions)

## License

Licensed under the [Apache License, Version 2.0](https://github.com/walt-id/waltid-nftkit/blob/main/LICENSE)
