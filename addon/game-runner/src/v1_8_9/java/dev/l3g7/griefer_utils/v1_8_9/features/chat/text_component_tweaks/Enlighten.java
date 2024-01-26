/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import static net.minecraft.util.EnumChatFormatting.*;

@Singleton
public class Enlighten extends TextComponentTweak {


	private final SwitchSetting enlightenLightGray = SwitchSetting.create()
		.name("Hellgrau aufhellen")
		.description("Ob hellgraue Texte zu weißen aufgehellt werden soll.")
		.icon(new ItemStack(Blocks.wool, 1, 8))
		.callback(TabListEvent::updatePlayerInfoList);

	private final DropDownSetting<GrayMode> enlightenGray = DropDownSetting.create(GrayMode.class)
		.name("Grau zu ...")
		.description("Zu welcher Farbe graue Texte aufgehellt werden soll.")
		.icon(new ItemStack(Blocks.wool, 1, 7))
		.defaultValue(GrayMode.GRAY)
		.callback(TabListEvent::updatePlayerInfoList);

	private final DropDownSetting<BlackMode> enlightenBlack = DropDownSetting.create(BlackMode.class)
		.name("Schwarz zu ...")
		.description("Zu welcher Farbe schwarze Texte aufgehellt werden soll.")
		.icon(new ItemStack(Blocks.wool, 1, 15))
		.defaultValue(BlackMode.BLACK)
		.callback(TabListEvent::updatePlayerInfoList);

	@Override
	public void init() {
		super.init();
		chat.description("Ob Texte im Chat aufgehellt werden sollen.");
		tab.description("Ob Texte in der Tabliste aufgehellt werden sollen.");
		item.description("Ob Texte in Item-Beschreibungen aufgehellt werden sollen.");
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Aufhellen")
		.description("Hellt dunkle Texte auf.")
		.icon("light_bulb")
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(enlightenLightGray, enlightenGray, enlightenBlack,
			HeaderSetting.create().scale(.4).entryHeight(10),
			chat, tab, item);

	@Override
	void modify(IChatComponent component) {
		// Enlighten IChatComponent
		if (!component.getUnformattedTextForChat().equals("\u2503 ")) {
			if (component.getChatStyle().getColor() == BLACK)
				component.setChatStyle(component.getChatStyle().setColor(enlightenBlack.get().formatting));
			else if (component.getChatStyle().getColor() == EnumChatFormatting.DARK_GRAY)
				component.setChatStyle(component.getChatStyle().setColor(enlightenGray.get().formatting));
			else if (component.getChatStyle().getColor() == GRAY)
				component.setChatStyle(component.getChatStyle().setColor(enlightenLightGray.get() ? WHITE : GRAY));
		}
		component.getSiblings().forEach(this::modify);
	}

	@Override
	String modify(String message) {
		// Enlighten String
		// Can't use replace, so I have to iterate through all
		char[] result = new char[message.length()];
		boolean isColor = false;
		int index = -1;
		for (char c : message.toCharArray()) {
			index++;
			result[index] = c;
			if (c == '§') {
				isColor = true;
				continue;
			}
			if (isColor) {
				if (c == '0') {
					result[index] = enlightenBlack.get().formatting.toString().charAt(1);
				} else if (c == '8') {
					result[index] = enlightenGray.get().formatting.toString().charAt(1);
				} else if (c == '7') {
					result[index] = enlightenLightGray.get() ? 'f' : '7';
				}
				isColor = false;
			}
		}
		return new String(result).replaceAll("§[f70](§l)?\u2503", "§8$1\u2503"); // Don't enlighten delimiter
	}

	private enum GrayMode implements Named {

		GRAY("Grau", EnumChatFormatting.DARK_GRAY),
		LIGHT("Hellgrau", EnumChatFormatting.GRAY),
		WHITE("Weiß", EnumChatFormatting.WHITE);

		private final String name;
		private final EnumChatFormatting formatting;

		GrayMode(String name, EnumChatFormatting formatting) {
			this.name = name;
			this.formatting = formatting;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	private enum BlackMode implements Named {

		BLACK("Schwarz", EnumChatFormatting.BLACK),
		GRAY("Grau", EnumChatFormatting.DARK_GRAY),
		LIGHT("Hellgrau", EnumChatFormatting.GRAY),
		WHITE("Weiß", EnumChatFormatting.WHITE);

		private final String name;
		private final EnumChatFormatting formatting;

		BlackMode(String name, EnumChatFormatting formatting) {
			this.name = name;
			this.formatting = formatting;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
