rootProject.name = "GrieferUtils"

pluginManagement {
	val labyGradlePluginVersion = "0.8.1"
	buildscript {
        repositories {
            maven("https://maven.laby.net/api/v1/maven/release/")
			maven("https://jitpack.io/")
			maven("https://maven.neoforged.net/releases/")
			maven("https://maven.fabricmc.net/")
			gradlePluginPortal()
			mavenCentral()
        }

        dependencies {
            classpath("net.labymod.gradle", "common", labyGradlePluginVersion)
        }
    }
}

plugins.apply("net.labymod.labygradle.settings")
