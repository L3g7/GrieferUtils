version = "0.0.0"

plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    compileOnly(fileTree("../libs"))
    implementation("net.minecraft:launchwrapper:1.12")

    // Required because the core package actually doesn't include LabyMod 4's core, only its api.
    // I can't just use the latest version as LabyMod sometimes publishes unstable versions with breaking changes.
    // I can't get it from game-runner as game-runner is dependent on core and its build script model must be fully prepared to get its dependencies.
    // I hope that's just a temporary fix
    implementation("net.labymod.labymod4:core:4.1.25-internal-36fcef79")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}