import org.apache.tools.ant.util.Base64Converter
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.castAll

plugins {
    kotlin("multiplatform") version "1.8.21"
    id("dev.petuska.npm.publish") version "3.3.1"
    `maven-publish`
}

group = "id.walt"
version = "1.SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(16)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        nodejs() {
            generateTypeScriptDefinitions()
        }
        binaries.library()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    val kryptoVersion = "4.0.1"

    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("com.soywiz.korlibs.krypto:krypto:$kryptoVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.5.5")

                implementation("io.kotest:kotest-assertions-json:5.5.5")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.nimbusds:nimbus-jose-jwt:9.30.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.13.2")

                implementation("io.kotest:kotest-runner-junit5:5.5.5")
                implementation("io.kotest:kotest-assertions-core:5.5.5")
                implementation("io.kotest:kotest-assertions-json:5.5.5")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(npm("jose", "~4.14.4"))
            }
        }
        val jsTest by getting {

        }
        val nativeMain by getting
        val nativeTest by getting
    }

    publishing {
        repositories {
            maven {
                url = uri("https://maven.walt.id/repository/waltid-ssi-kit/")
                val envUsername = System.getenv("MAVEN_USERNAME")
                val envPassword = System.getenv("MAVEN_PASSWORD")

                val usernameFile = File("secret_maven_username.txt")
                val passwordFile = File("secret_maven_password.txt")

                val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
                //println("Deploy username length: ${secretMavenUsername.length}")
                val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }

                //if (secretMavenPassword.isBlank()) {
                //   println("WARNING: Password is blank!")
                //}

                credentials {
                    username = secretMavenUsername
                    password = secretMavenPassword
                }
            }
        }
    }
}

npmPublish {
    registries {
        val envToken = System.getenv("NPM_TOKEN")
        val npmTokenFile = File("secret_npm_token.txt")
        val secretNpmToken = envToken ?: npmTokenFile.let { if (it.isFile) it.readLines().first() else "" }
        println("NPM token")
        println(secretNpmToken.substring(0,8))
        if(Regex("\\d+.\\d+.\\d+").matches(version.get()) && secretNpmToken.isNotEmpty()) {
            readme.set(File("README.md"))
            register("npmjs") {
                uri.set(uri("https://registry.npmjs.org"))
                authToken.set(secretNpmToken)
            }
        }
    }
}
