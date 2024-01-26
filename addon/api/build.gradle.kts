version = "0.0.0"

plugins {
    id("java-library")
}

dependencies {
    labyApi("api")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.INTERFACE
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}