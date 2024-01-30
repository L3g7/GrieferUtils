version = "0.0.0"

plugins {
	id("de.undercouch.download") version "5.3.0"
}

dependencies {
	compileOnly(fileTree("../libs"))
	compileOnly(fileTree("libs"))

	// mXparser
	implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.1.0")

	// ZXing (QR Code Reader)
	implementation("com.google.zxing:core:3.5.1")
}

tasks.build {
	dependsOn("downloadLibs")
}

tasks.register("downloadLibs") {
	mkdir("libs")

	download.run {
		src(arrayOf(
			"https://mediafilez.forgecdn.net/files/2279/147/Schematica-1.8.9-1.7.7.143-universal.jar", // Schematica
			"https://mediafilez.forgecdn.net/files/2272/942/LunatriusCore-1.8.9-1.1.2.32-universal.jar" // LunatriusCore
		))
		dest("libs/")
		onlyIfModified(true)
		quiet(true)
	}
}
