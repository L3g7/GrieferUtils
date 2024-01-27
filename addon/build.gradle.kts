import net.labymod.gradle.core.addon.info.AddonMeta

plugins {
	id("java-library")
	id("net.labymod.gradle")
	id("net.labymod.gradle.addon")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

labyMod {
	defaultPackageName = "dev.l3g7.griefer_utils"
	addonInfo {
		namespace = "griefer_utils"
		displayName = "GrieferUtils"
		author = "L3g7, L3g73"
		description = "§cFalls du das hier liest, gab es einen Fehler."
		minecraftVersion = "1.8.9"
		version = providers.gradleProperty("version").get()
		meta(AddonMeta.RESTART_REQUIRED)
	}

	minecraft {
		registerVersions(
			"1.8.9"
		) { version, provider ->
			configureRun(provider, version)
		}

		subprojects.forEach {
			if (it.name != "game-runner") {
				filter(it.name)
			}
		}
	}

	addonDev {
		productionRelease()
	}
}

subprojects {
	plugins.apply("java-library")
	plugins.apply("net.labymod.gradle")
	plugins.apply("net.labymod.gradle.addon")

	repositories {
		maven("https://libraries.minecraft.net/")
		maven("https://repo.spongepowered.org/repository/maven-public/")
	}
}

project(":game-runner") {
	dependencies {
		compileOnly(fileTree("../libs"))
	}
}

fun configureRun(provider: net.labymod.gradle.core.minecraft.provider.VersionProvider, gameVersion: String) {
	provider.runConfiguration {
		mainClass = "net.minecraft.launchwrapper.Launch"
		jvmArgs("-Dnet.labymod.running-version=${gameVersion}")
		jvmArgs("-Dmixin.debug=true")
		jvmArgs("-Dnet.labymod.debugging.all=true")
		jvmArgs("-Dmixin.env.disableRefMap=true")

		args("--tweakClass", "net.labymod.core.loader.vanilla.launchwrapper.LabyModLaunchWrapperTweaker")
		args("--labymod-dev-environment", "true")
		args("--addon-dev-environment", "true")
	}

	provider.javaVersion = JavaVersion.VERSION_17

	provider.mixin {
		val mixinMinVersion = when (gameVersion) {
			"1.8.9", "1.12.2", "1.16.5" -> {
				"0.6.6"
			}

			else -> {
				"0.8.2"
			}
		}

		minVersion = mixinMinVersion
	}
}
