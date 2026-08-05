package me.averi.pwoxy

import io.netty.channel.Channel
import io.netty.handler.proxy.Socks5ProxyHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.network.chat.Component.translatable
import net.minecraft.resources.Identifier.fromNamespaceAndPath
import java.net.InetSocketAddress

object Pwoxy {
  private val IP_REGEX = run {
    val uint8 = Regex("(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)")
    val uint16 = Regex("(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]?\\d{1,4})")
    Regex("^(?<address>($uint8\\.){3}$uint8)(:(?<port>$uint16))?$")
  }

  @get:JvmName("config")
  val config by lazy { Config.get() }

  @get:JvmName("mc")
  val mc: Minecraft get() = Minecraft.getInstance()

  @get:JvmName("createMenuButton")
  private val menuButton
    get() = SpriteIconButton.builder(translatable("options.pwoxy"), { mc.setScreen(ConfigScreen) }, true).width(20)
      .sprite(fromNamespaceAndPath("pwoxy", "textures/icons/config"), 15, 15).build()

  @JvmStatic
  fun postInitChannel(channel: Channel) {
    val hostMatch = IP_REGEX.matchEntire(config.host) ?: return
    val host =
      InetSocketAddress(hostMatch.groups["address"]!!.value, hostMatch.groups["port"]?.value?.toIntOrNull() ?: 1080)
    channel.pipeline().addFirst(Socks5ProxyHandler(host, config.login, config.password))
  }

  @JvmStatic
  fun postTitleScreenInit(screen: TitleScreen) {
    val (x, y) = screen.width / 2 + 104 to screen.height / 4 + 72
    screen.addRenderableWidget(menuButton).setPosition(x, y)
  }
}
