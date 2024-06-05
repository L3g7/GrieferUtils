/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@Singleton
public class NoMagicText extends TextComponentTweak {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Magischen Text deaktivieren")
		.description("Deaktiviert den magischen / verschleierten / verschlüsselten Stil in Chatnachrichten.")
		.icon(Items.blaze_powder)
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(chat, tab, item);

	@Override
	public void init() {
		super.init();
		chat.description("Ob magische Texte im Chat deaktiviert werden sollen.");
		tab.description("Ob magische Texte in der Tabliste deaktiviert werden sollen.");
		item.description("Ob magische Texte in Item-Beschreibungen deaktiviert werden sollen.");
	}

	@Override
	void modify(IChatComponent component) {
		component.getChatStyle().setObfuscated(false);
		if (component instanceof ChatComponentText)
			Reflection.set(component, "text", component.getUnformattedTextForChat().replace("§k", ""));

		component.getSiblings().forEach(this::modify);
	}

	@Override
	String modify(String message) {
		return message.replace("§k", "");
	}

}
