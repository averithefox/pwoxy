import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("net.fabricmc.fabric-loom-remap")
  `maven-publish`
  id("org.jetbrains.kotlin.jvm") version "2.3.21"
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

dependencies {
  minecraft("com.mojang:minecraft:$minecraftVersion")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

  modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
  modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

  modLocalRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

loom {
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
  processResources {
    filesMatching("fabric.mod.json") {
      expand(project.properties)
    }
  }

  withType<JavaCompile>().configureEach {
    options.release = 21
  }

  jar {
    val projectName = project.name
    inputs.property("projectName", projectName)

    from("LICENSE") {
      rename { "${it}_$projectName" }
    }
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

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }

  repositories {
    mavenLocal()
  }
}
