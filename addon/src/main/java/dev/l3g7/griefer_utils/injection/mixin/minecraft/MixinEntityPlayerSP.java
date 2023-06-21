package dev.l3g7.griefer_utils.injection.mixin.minecraft;

import dev.l3g7.griefer_utils.event.events.MessageEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void injectSendChatMessage(String message, CallbackInfo ci) {
		if (MinecraftForge.EVENT_BUS.post(new MessageEvent.MessageAboutToBeSentEvent(message)))
			ci.cancel();
	}

}
