/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu.ChatMenuBridge;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.CopyTextEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.EntryDisplaySetting;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby3.chat_line_util.ChatLineUtil;
import net.labymod.core_implementation.mc18.MinecraftImplementation;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class ChatMenuLaby3 implements ChatMenuBridge {

	@Override
	public EntryDisplaySetting createEntry(ChatMenuEntry entry) {
		return new EntryDisplaySettingLaby3(entry);
	}

	@Override
	public SwitchSetting createCopyEntry(CopyTextEntry target) {
		SwitchSetting settingContainer = SwitchSetting.create()
			.name(target.name)
			.subSettings(target.copyFormat, target.modifiedMessage);

		return target.setIcon(new CopyDisplaySettingLaby3(settingContainer));
	}

	@Override
	public boolean isChatOpen() {
		return mc().currentScreen instanceof GuiChat;
	}

	@Override
	public Pair<IChatComponent, IChatComponent> getHoveredComponent() {
		return ChatLineUtil.getHoveredComponent();
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = MinecraftImplementation.class, remap = false)
	private static class MixinMinecraftImplementation {

		@Inject(method = "getClickEventValue", at = @At("HEAD"), cancellable = true)
		public void injectGetClickEventValue(int x, int y, CallbackInfoReturnable<String> cir) {
			if (ChatMenu.get().isEnabled())
				cir.setReturnValue(null);
		}

	}

}