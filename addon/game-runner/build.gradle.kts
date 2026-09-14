version = "0.0.0"

plugins {
	id("de.undercouch.download") version "5.3.0"
}

repositories {
	maven("https://maven.pymdk.dev")
}

dependencies {
	compileOnly(fileTree("../libs"))

	implementation("org.jetbrains:annotations:24.1.0")

	// mXparser
	implementation("org.mariuszgromada.math:MathParser.org-mXparser:6.1.0")

	// ZXing (QR Code Reader)
	implementation("com.google.zxing:core:3.5.4")

	// Brigadier (Command dispatcher)
	implementation("com.mojang:brigadier:1.0.18")

	// PyMDK Mapper (Reflection, LabyMod 3)
	implementation("dev.pymdk:mapper:2.0.0")
}
