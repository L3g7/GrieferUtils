version = "0.1.0"

plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))
//	implementation project(':core')
	compileOnly(fileTree("libs"))
	implementation("net.minecraft:launchwrapper:1.12")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_17
}
