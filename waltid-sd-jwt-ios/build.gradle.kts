listOf("iphoneos", "iphonesimulator").forEach { sdk ->
    tasks.create<Exec>("build${sdk.capitalize()}") {
        group = "build"

        commandLine(
            "xcodebuild",
            "-project", "waltid-sd-jwt-ios.xcodeproj",
            "-scheme","waltid-sd-jwt-ios",
            "-sdk", sdk,
            "-configuration","Release"
        )
        workingDir(projectDir)

        inputs.files(
            fileTree("$projectDir/waltid-sd-jwt-ios.xcodeproj") { exclude("**/xcuserdata") },
            fileTree("$projectDir/waltid-sd-jwt-ios")
        )
        outputs.files(
            fileTree("$projectDir/build/Release-${sdk}")
        )
    }
}

tasks.create<Delete>("clean") {
    group = "build"

    delete("$projectDir/build")
}
