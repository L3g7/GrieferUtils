/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import net.labymod.utils.manager.TagManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@ExclusiveTo(LABY_3)
public class MessageModifyHandler {

	private static IChatComponent lastMessage;
	private static boolean isFromPacket;

	@ExclusiveTo(LABY_3)
	@Mixin(value = ForgeEventFactory.class, remap = false)
	private static class MixinForgeEventFactory {

		@Inject(method = "onClientChat", at = @At("HEAD"))
		private static void injectOnClientChatHead(byte type, IChatComponent message, CallbackInfoReturnable<IChatComponent> cir) {
			lastMessage = message;
		}

	    @Inject(method = "onClientChat", at = @At("RETURN"))
	    private static void injectOnClientChat(byte type, IChatComponent message, CallbackInfoReturnable<IChatComponent> cir) {
			if (type != 2 && cir.getReturnValue() != null)
				isFromPacket = true;
	    }

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = TagManager.class, remap = false)
	private static class MixinTagManager {

		@Inject(method = "tagComponent", at = @At("HEAD"))
		private static void injectTagComponentHead(Object chatComponent, CallbackInfoReturnable<Object> cir) {
			if (!isFromPacket)
				lastMessage = ((IChatComponent) chatComponent).createCopy();
			else
				isFromPacket = false;
		}

		@ModifyVariable(method = "tagComponent", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;getConfigManager()Lnet/labymod/utils/manager/ConfigManager;", shift = At.Shift.BEFORE, ordinal = 0), ordinal = 0, argsOnly = true)
		private static Object injectTagComponentReturn(Object value) {
			return ((LabyBridgeImpl) labyBridge).messageModifyConsumer.apply(lastMessage, value);
		}

	}

}
