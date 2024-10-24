/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.temp;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.labymod.laby3.bridges.LabyBridgeImpl;
import net.labymod.core.LabyModCore;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.ingamechat.renderer.MessageData;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.utils.manager.TagManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@ExclusiveTo(LABY_3)
@Mixin(value = TagManager.class, remap = false)
public class TempMessageModifyHandler {

	@Inject(method = "tagComponent", at = @At("RETURN"), cancellable = true)
	private static void injectTagComponent(Object chatComponent, CallbackInfoReturnable<Object> cir) {
		IChatComponent original = (IChatComponent) chatComponent;
		IChatComponent modified = (IChatComponent) cir.getReturnValue();

		modified = (IChatComponent) ((LabyBridgeImpl) labyBridge).messageModifyConsumer
			.apply(original, modified);

		cir.setReturnValue(modified);
	}

}
