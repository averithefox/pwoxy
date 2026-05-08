import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("net.fabricmc.fabric-loom-remap")
  kotlin("jvm") version "2.3.21"
  kotlin("plugin.serialization") version "2.3.21"
  id("com.gradleup.shadow") version "9.4.1"
  id("com.github.jmongard.git-semver-plugin") version "0.18.0"
}

semver {
	groupVersionIncrements = false
}

version = semver.infoVersion
val minecraftVersion: String by project
val loaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project

repositories {
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImplementation by configurations.creating {
  configurations.implementation {
    extendsFrom(this@creating)
  }
}

dependencies {
  minecraft("com.mojang:minecraft:$minecraftVersion")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

  modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
  modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

  modLocalRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")

  shadowImplementation("io.netty:netty-handler-proxy:4.2.12.Final")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}

loom {
  accessWidenerPath = file("src/main/resources/pwoxy.accessWidener")

  runConfigs.named("client") {
    isIdeConfigGenerated = true
    vmArgs.addAll(
      arrayOf(
        "-Dmixin.debug.export=true",
        "-Ddevauth.enabled=true",
        "-Ddevauth.account=main"
      )
    )
  }
}

afterEvaluate {
  loom.runs.named("client") {
    vmArg("-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}")
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    options.release = 21
  }

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

    exclude("META-INF/maven/")
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
