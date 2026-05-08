package me.averi.pwoxy

import me.averi.pwoxy.Pwoxy.mc
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import kotlin.reflect.KProperty

object ConfigScreen : Screen(translatable("options.pwoxy")) {
  private val IP_SPLIT_REGEX = Regex("(?<=[.:])|(?=[.:])")
  private val NUMBER_REGEX = Regex("^\\d+$")

  private val widgets = arrayListOf<AbstractWidget>()
  private val hostBox by EditBox(mc.font, 0, 0, 150, 18, translatable("pwoxy.input.host"))
  private val loginBox by EditBox(mc.font, 0, 0, 150, 18, translatable("pwoxy.input.login"))
  private val passwordBox by EditBox(mc.font, 0, 0, 150, 18, translatable("pwoxy.input.password"))

  init {
    widgets.forEach { if (it is EditBox) it.setMaxLength(Int.MAX_VALUE) }
    hostBox.addFormatter { stringBehindCursor, firstCharacterIndex ->
      val string = hostBox.value

      val side = when {
        firstCharacterIndex == 0 -> Side.LEFT
        stringBehindCursor == string.substring(0, firstCharacterIndex) -> Side.LEFT
        stringBehindCursor == string.substring(firstCharacterIndex) -> Side.RIGHT
        else -> null // ???
      } ?: return@addFormatter null

      val parts = string.split(IP_SPLIT_REGEX)
      val partialParts = stringBehindCursor.split(IP_SPLIT_REGEX)
      val portDelimIdx = parts.indexOf(":")

      Component.empty().apply {
        var octets = 0
        var delimError = false
        partialParts.forEachIndexed { partialIndex, partialPart ->
          val index = partialIndex + when (side) {
            Side.LEFT -> 0
            Side.RIGHT -> parts.size - partialParts.size
          }

          val style = when (val part = parts[index]) {
            ".", ":" -> {
              val (trailing, leading) = parts.getOrNull(index - 1)?.toIntOrNull() to parts.getOrNull(index + 1)
                ?.toIntOrNull()
              when {
                // delimiter error upstream
                delimError -> ChatFormatting.RED
                // nothing to connect to, out of place
                trailing == null || leading == null -> {
                  delimError = true
                  ChatFormatting.RED
                }
                // no need for new octets
                part == "." && octets >= 4 -> {
                  delimError = true
                  ChatFormatting.RED
                }
                // second port???
                part == ":" && index != portDelimIdx -> {
                  delimError = true
                  ChatFormatting.RED
                }
                else -> ChatFormatting.GRAY
              }
            }

            else -> {
              val isPort = portDelimIdx != -1 && index > portDelimIdx
              val bits = if (isPort) 16 else 8
              val max = 1 shl bits
              val num = part.toIntOrNull()?.takeIf { it in 0 until max }

              ++octets
              when {
                // delimiter error upstream
                delimError -> ChatFormatting.RED
                // not a valid number
                num == null || !NUMBER_REGEX.matches(part) -> ChatFormatting.RED
                // multiple-character zero literal (trailing zeros)
                num == 0 && part.length > 1 -> ChatFormatting.RED
                // too many octets
                !isPort && octets > 4 -> ChatFormatting.RED
                // second port????
                isPort && index > portDelimIdx + 1 -> ChatFormatting.RED
                else -> null
              }
            }
          }

          append(Component.literal(partialPart).apply { if (style != null) withStyle(style) })
        }
      }.visualOrderText
    }
  }

  override fun init() {
    val spacing = mc.font.lineHeight + 4
    val totalHeight = widgets.sumOf { it.height + spacing } - spacing
    var (x, y) = ((width - widgets.maxOf { it.width }) / 2f).toInt() to ((height - totalHeight) / 2f).toInt()

    widgets.forEach {
      addRenderableWidget(it)
      it.setPosition(x, y)
      y += it.height + spacing
    }

    hostBox.value = Pwoxy.config.host
    loginBox.value = Pwoxy.config.login
    passwordBox.value = Pwoxy.config.password
  }

  override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
    super.render(ctx, mouseX, mouseY, tickDelta)

    listOf(hostBox, loginBox, passwordBox).forEach {
      ctx.drawString(mc.font, it.message, it.x, it.y - mc.font.lineHeight, 0xff_ffffff.toInt())
    }
  }

  override fun onClose() {
    Pwoxy.config.host = hostBox.value
    Pwoxy.config.login = loginBox.value
    Pwoxy.config.password = passwordBox.value
    Pwoxy.config.save()

    super.onClose()
  }

  @Suppress("unused")
  private operator fun <T : AbstractWidget> T.provideDelegate(thisRef: ConfigScreen, property: KProperty<*>): T {
    thisRef.widgets.add(this)
    return this
  }

  @Suppress("unused")
  private operator fun <T : AbstractWidget> T.getValue(thisRef: ConfigScreen, property: KProperty<*>): T {
    return this
  }

  private enum class Side {
    LEFT, RIGHT
  }
}
