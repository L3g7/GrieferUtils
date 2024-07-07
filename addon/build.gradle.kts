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
		description = "Post-processor failed to apply :(" // Overwritten by runBuildPostProcessor task
		minecraftVersion = "1.8.9"
		version = providers.gradleProperty("version").get()
		meta(AddonMeta.RESTART_REQUIRED, AddonMeta.BACKGROUND)
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

	provider.javaVersion = JavaVersion.VERSION_21

	provider.mixin {
		minVersion = "0.6.6"
	}
}
