pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		gradlePluginPortal()
		mavenCentral()
	}

	plugins {
		id("net.fabricmc.fabric-loom") version providers.gradleProperty("loomVersion")
	}
}

rootProject.name = "pwoxy"
