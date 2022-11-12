package dev.l3g7.griefer_utils.mixin.mixins;

import dev.l3g7.griefer_utils.event.events.DisplayNameGetEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void injectGetDisplayName(CallbackInfoReturnable<IChatComponent> cir) {
		cir.setReturnValue(DisplayNameGetEvent.post((EntityPlayer) (Object) this, cir.getReturnValue()));
	}

}
