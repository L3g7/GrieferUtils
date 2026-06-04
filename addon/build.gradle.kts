import net.labymod.labygradle.common.internal.labymod.addon.model.AddonMeta
import java.util.Properties

plugins {
	id("net.labymod.labygradle")
	id("net.labymod.labygradle.addon")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

labyMod {
	defaultPackageName = "dev.l3g7.griefer_utils"
	addonInfo {
		namespace = "griefer_utils"
		displayName = "GrieferUtils"
		author = "L3g7, L3g73"
		description = "Post-processor failed to apply :(" // Overwritten by runBuildPostProcessor task
		minecraftVersion = "1.8.9"
		version = providers.gradleProperty("version").get()
		meta(AddonMeta.RESTART_REQUIRED, AddonMeta.BACKGROUND)
	}

	minecraft {
		registerVersion("1.8.9") {}
	}

}

subprojects {
	plugins.apply("net.labymod.labygradle")
	plugins.apply("net.labymod.labygradle.addon")
}

tasks.createReleaseJar.get().finalizedBy("runBuildPostProcessor")

tasks.register("runBuildPostProcessor", JavaExec::class) {
	dependsOn("game-runner:compileV1_8_9Java", "game-runner:v1_8_9Jar")

	doFirst {
		var gameRunner = subprojects.first { p -> p.name == "game-runner" }
		classpath(
			gameRunner.layout.buildDirectory.get().toString() + "/classes/java/v1_8_9",
			gameRunner.configurations["v1_8_9RuntimeClasspath"].resolve()
		)
	}

	var props = Properties()
	file("gradle.properties").inputStream().use { props.load(it) }

	group = "GrieferUtils"
	jvmArgs(
		"-Dgriefer_utils.version=" + props.getProperty("version"),
		"-Dgriefer_utils.debug=" + props.getProperty("debug"),
		"-Dgriefer_utils.beta=" + props.getProperty("beta"),
		"-Dgriefer_utils.preprocessing=true"
	)
	mainClass.set("dev.l3g7.griefer_utils.post_processor.BuildPostProcessor")
}
