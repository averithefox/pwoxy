plugins {
  kotlin("jvm") version "2.4.10"
  id("net.fabricmc.fabric-loom")
  id("com.gradleup.shadow") version "9.4.1"
  kotlin("plugin.serialization") version "2.3.21"
  id("com.github.jmongard.git-semver-plugin") version "0.18.0"
}

semver {
	groupVersionIncrements = false
}

version = semver.infoVersion

repositories {
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImplementation = configurations.create("shadowImplementation") {
  configurations.implementation {
    extendsFrom(this@create)
  }
}

dependencies {
  minecraft("com.mojang:minecraft:${property("minecraftVersion")}")
  implementation("net.fabricmc:fabric-loader:${property("loaderVersion")}")

  implementation("net.fabricmc.fabric-api:fabric-api:${property("fabricApiVersion")}")
  implementation("net.fabricmc:fabric-language-kotlin:${property("fabricKotlinVersion")}")

  localRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
  shadowImplementation("io.netty:netty-handler-proxy:4.2.12.Final") {
    isTransitive = false
  }
  shadowImplementation("io.netty:netty-codec-socks:4.2.12.Final") {
    isTransitive = false
  }
}

loom {
  accessWidenerPath = file("src/main/resources/pwoxy.accessWidener")

  runConfigs.named("client") {
    generateRunConfig = true
    jvmArguments.run {
      add("-Dmixin.debug.export=true")
      add("-Ddevauth.enabled=true")
      add("-Ddevauth.account=main")
      add("-XX:+AllowEnhancedClassRedefinition")
      add("-XX:+IgnoreUnrecognizedVMOptions")
    }
  }
}

afterEvaluate {
  loom.runConfigs.named("client") {
    jvmArguments.add("-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}")
  }
}

tasks {
  processResources {
    from(rootProject.file("LICENSE")) {
      rename { "${it}_${project.name}" }
    }

    filesMatching("fabric.mod.json") {
      expand(project.properties)
    }
  }

  jar {
    archiveClassifier = "nodeps"
    destinationDirectory = layout.buildDirectory.dir("badjars")
  }

  shadowJar {
    archiveClassifier = null

    configurations = listOf(shadowImplementation)

    minimize()

    exclude("META-INF/maven/")
  }
}

kotlin {
  jvmToolchain(25)
}
