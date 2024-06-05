import net.labymod.gradle.core.dsl.getBuildDirectory
import java.util.Properties

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

tasks.register("run", JavaExec::class) {
	group = "Application"
	description = "Runs this project as a JVM application."
	classpath(
		getBuildDirectory() + "/build/classes/java/v1_8_9",
		System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.10.1/b3add478d4382b78ea20b1671390a858002feb6c/gson-2.10.1.jar",
		System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.7/73d7b3086e14beb604ced229c302feff6449723/asm-9.7.jar",
		System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-tree/9.7/e446a17b175bfb733b87c5c2560ccb4e57d69f1a/asm-tree-9.7.jar"
	)
	jvmArgs(
		"-Dgriefer_utils.version=" + props.getProperty("version"),
		"-Dgriefer_utils.debug=" + props.getProperty("debug")
	)
	mainClass.set("dev.l3g7.griefer_utils.post_processor.PostProcessor")
}
