package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

public class RefreshResourcesEvent extends Event {

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Inject(method = "refreshResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IReloadableResourceManager;reloadResources(Ljava/util/List;)V", shift = AFTER))
		private void injectRefreshResources(CallbackInfo ci) {
			new RefreshResourcesEvent().fire();
		}

	}

}
