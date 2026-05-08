package me.averi.pwoxy.mixin;

import io.netty.channel.Channel;
import me.averi.pwoxy.Pwoxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/Connection$1")
public class ConnectionMixin {
  @Inject(method = "initChannel", at = @At("TAIL"))
  private void postInitChannel(Channel channel, CallbackInfo ci) {
    Pwoxy.postInitChannel(channel);
  }
}
