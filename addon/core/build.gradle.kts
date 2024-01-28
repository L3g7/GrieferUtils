version = "0.0.0"

plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    compileOnly(fileTree("../libs"))
    implementation("net.minecraft:launchwrapper:1.12")
    implementation("net.labymod.labymod4:core:+")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}