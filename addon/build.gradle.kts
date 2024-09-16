import net.labymod.labygradle.common.internal.labymod.addon.model.AddonMeta

plugins {
	id("net.labymod.labygradle")
	id("net.labymod.labygradle.addon")
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
		registerVersion("1.8.9") {}
	}

}

subprojects {
	plugins.apply("net.labymod.labygradle")
	plugins.apply("net.labymod.labygradle.addon")
}
