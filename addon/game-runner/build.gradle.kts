import java.util.*

version = "0.0.0"

plugins {
	id("de.undercouch.download") version "5.3.0"
}

dependencies {
	compileOnly(fileTree("../libs"))

	implementation("org.jetbrains:annotations:24.1.0")

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
		src(
			arrayOf(
				"https://mediafilez.forgecdn.net/files/2279/147/Schematica-1.8.9-1.7.7.143-universal.jar", // Schematica
				"https://mediafilez.forgecdn.net/files/2272/942/LunatriusCore-1.8.9-1.1.2.32-universal.jar" // LunatriusCore
			)
		)
		dest("libs/")
		onlyIfModified(true)
		quiet(true)
	}
}

var props = Properties()
file("../gradle.properties").inputStream().use { props.load(it) }

tasks.build.get().finalizedBy("runBuildPostProcessor")

tasks.register("runBuildPostProcessor", JavaExec::class) {
	dependsOn("compileV1_8_9Java", "v1_8_9Jar", "jar")

	doFirst {
		classpath(
			project.layout.buildDirectory.get().toString() + "/classes/java/v1_8_9",
			configurations["v1_8_9RuntimeClasspath"].resolve()
		)
	}

	group = "GrieferUtils"
	jvmArgs(
		"-Dgriefer_utils.version=" + props.getProperty("version"),
		"-Dgriefer_utils.debug=" + props.getProperty("debug")
	)
	mainClass.set("dev.l3g7.griefer_utils.post_processor.BuildPostProcessor")
}
