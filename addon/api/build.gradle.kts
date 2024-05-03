version = "0.0.0"

plugins {
    id("java-library")
}

dependencies {
    labyApi("api")
	implementation("com.google.zxing:core:3.5.1") // ZXing (QR Code Reader)
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.INTERFACE
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}