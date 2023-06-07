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

* Sign SD-JWT tokens
* Present SD-JWT tokens with selection of disclosed and undisclosed payload fields
* Parse and verify SD-JWT tokens, resolving original payload with disclosed fields
* Integrate with your choice of framework or library, on your platform, for cryptography and key management
* Multiplatform support: Java/JVM, JavaScript, Native 
* [Usage with Java or Kotlin on JVM](#usage-with-java--kotlin-and-jvm)

### Further information

Checkout the [documentation regarding SD-JWTs](https://docs.walt.id/v/ssikit/concepts/selective-disclosure), to find out more.

## What is the SD-JWT library?

This libary implements the **Selective Disclosure JWT (SD-JWT)** specification:  [draft-ietf-oauth-selective-disclosure-jwt-04](https://datatracker.ietf.org/doc/draft-ietf-oauth-selective-disclosure-jwt/04/).

### Features

* **Create and sign** SD-JWT tokens
  * Choose selectively disclosable payload fields (SD fields)
  * Create digests for SD fields and insert into JWT body payload
  * Create and append encoded disclosure strings for SD fields to JWT token
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

## Usage with Java / Kotlin and JVM

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

## Example
TODO: Desribe code examples

```kotlin
fun main() {
    

    }
```


## Join the community

* Connect and get the latest updates: [Discord](https://discord.gg/AW8AgqJthZ) | [Newsletter](https://walt.id/newsletter) | [YouTube](https://www.youtube.com/channel/UCXfOzrv3PIvmur_CmwwmdLA) | [Twitter](https://mobile.twitter.com/walt_id)
* Get help, request features and report bugs: [GitHub Discussions](https://github.com/walt-id/.github/discussions)

## License

Licensed under the [Apache License, Version 2.0](https://github.com/walt-id/waltid-nftkit/blob/main/LICENSE)
