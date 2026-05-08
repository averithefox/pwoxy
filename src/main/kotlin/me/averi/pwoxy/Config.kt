package me.averi.pwoxy

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.io.File

@Serializable
data class Config(var host: String, var login: String, var password: String) {
  fun save() {
    val serialized = Json.encodeToString(this)
    file.writeText(serialized)
  }

  companion object {
    @get:JvmName("file")
    private val file by lazy { File("${FabricLoader.getInstance().configDir}/pwoxy.json") }

    @get:JvmName("default")
    val default get() = Config("", "", "")

    fun get(): Config {
      if (!file.exists()) return default
      return Json.decodeFromString<Config>(file.readText())
    }
  }
}
