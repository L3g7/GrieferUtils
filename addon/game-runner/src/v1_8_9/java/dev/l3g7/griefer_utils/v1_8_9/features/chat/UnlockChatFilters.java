/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static java.lang.Integer.MAX_VALUE;

@Singleton
@ExclusiveTo(LABY_3)
public class UnlockChatFilters extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chat-Filter-Länge entsperren")
		.description("Erhöht die maximale Länge von Chat Filtern.")
		.icon("long_speech_bubble");

	@Mixin(GuiChatFilter.class)
	private static class MixinGuiChatFilter {

		@ModifyArg(method = "drawElementTextField", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;setMaxStringLength(I)V"))
		public int injectInitGui(int previousLength) {
			return FileProvider.getSingleton(UnlockChatFilters.class).isEnabled() ? MAX_VALUE : previousLength;
		}

	}

}