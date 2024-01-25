/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;

@Singleton
public class NoMagicText extends TextComponentTweak {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Magischen Text deaktivieren")
		.description("Deaktiviert den magischen / verschleierten / verschlüsselten Stil in Chatnachrichten.")
		.icon(Material.BLAZE_POWDER)
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
		component.getSiblings().forEach(this::modify);
	}

	@Override
	String modify(String message) {
		return message.replace("§k", "");
	}

}
