<div align="center">
 <h1>Kotlin Multiplatform SD-JWT library</h1>
 <span>by </span><a href="https://walt.id">walt.id</a>
  <p>Create JSON Web Tokens (JWTs) that support <b>Selective Disclosure</b><p>

<a href="https://walt.id/community">
<img src="https://img.shields.io/badge/Join-The Community-blue.svg?style=flat" alt="Join community!" />
</a>
<a href="https://twitter.com/intent/follow?screen_name=walt_id">
<img src="https://img.shields.io/twitter/follow/walt_id.svg?label=Follow%20@walt_id" alt="Follow @walt_id" />
</a>

</div>

## Getting Started

TODO: Add links to example code

* [Sign SD-JWT tokens](#create-and-sign-an-sd-jwt-using-the-nimbus-jwt-crypto-provider)
* [Present SD-JWT tokens with selection of disclosed and undisclosed payload fields](#present-an-sd-jwt)
* [Parse and verify SD-JWT tokens, resolving original payload with disclosed fields](#parse-and-verify-an-sd-jwt-using-the-nimbus-jwt-crypto-provider)
* [Integrate with your choice of framework or library, on your platform, for cryptography and key management](#integrate-with-custom-jwt-crypto-provider)
* Multiplatform support: Java/JVM, JavaScript, Native 
* [Usage with Maven or Gradle (JVM)](#usage-with-maven-or-gradle-jvm)

### Further information

Checkout the [documentation regarding SD-JWTs](https://docs.walt.id/v/ssikit/concepts/selective-disclosure), to find out more.

## What is the SD-JWT library?

This libary implements the **Selective Disclosure JWT (SD-JWT)** specification:  [draft-ietf-oauth-selective-disclosure-jwt-04](https://datatracker.ietf.org/doc/draft-ietf-oauth-selective-disclosure-jwt/04/).

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
* **Multiplatform support**: 
  * Java/JVM
  * JavaScript
  * Native

## Usage with Maven or Gradle (JVM)

**Maven / Gradle repository**:

`https://maven.walt.id/repository/waltid-ssi-kit/`

**Maven**

```xml
[...]
<repositories>
  <repository>
    <id>waltid-ssikit</id>
    <name>waltid-ssikit</name>
    <url>https://maven.walt.id/repository/waltid-ssi-kit/</url>
  </repository>
</repositories>
[...]
<dependency>
    <groupId>id.walt</groupId>
    <artifactId>waltid-sd-jwt-jvm</artifactId>
    <version>[ version ]</version>
</dependency>
```

**Gradle**

_Kotlin DSL_
```kotlin
[...]
repositories {
  maven("https://maven.walt.id/repository/waltid-ssi-kit/")
}
[...]
val sdJwtVersion = "1.2306071235.0"
[...]
dependencies {
  implementation("id.walt:waltid-sd-jwt-jvm:$sdJwtVersion")
}
```

## Examples
### Kotlin / JVM

#### Create and sign an SD-JWT using the Nimbus JWT crypto provider

This example creates and signs an SD-JWT, using the SimpleJWTCryptoProvider implementation, that's shipped with the waltid-sd-jwt library, which uses the `nimbus-jose-jwt` library for cryptographic operations. 

In this example we sign the JWT with the HS256 algorithm, and a shared secret, which we randomly generate.


```kotlin
fun main() {
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
    }
```

_Example output_

`eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsidWtxWFprV0VlcHRqYUN0eElnTEwtZDRuNUZidVNKQmY3TE9DVDF2TDY1TSJdfQ.NZ4iM0sksMg2BcxbyQEZEaWqM4tz00oJ3z_9aJMF7rM~WyJ2SWVrdjhHbGtfYmpsUjVLV1RCYndnIiwic3ViIiwiMTIzIl0`

#### Present an SD-JWT

#### Parse and verify an SD-JWT using the Nimbus JWT crypto provider

#### Integrate with custom JWT crypto provider




## Join the community

* Connect and get the latest updates: [Discord](https://discord.gg/AW8AgqJthZ) | [Newsletter](https://walt.id/newsletter) | [YouTube](https://www.youtube.com/channel/UCXfOzrv3PIvmur_CmwwmdLA) | [Twitter](https://mobile.twitter.com/walt_id)
* Get help, request features and report bugs: [GitHub Discussions](https://github.com/walt-id/.github/discussions)

## License

Licensed under the [Apache License, Version 2.0](https://github.com/walt-id/waltid-nftkit/blob/main/LICENSE)
