/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.chat.text_component_tweaks;

import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.event.events.network.TabListEvent.updatePlayerInfoList;
import static net.minecraft.util.EnumChatFormatting.*;

@Singleton
public class Enlighten extends TextComponentTweak {


	private final BooleanSetting enlightenLightGray = new BooleanSetting()
		.name("Hellgrau aufhellen")
		.icon(new ItemStack(Blocks.wool, 1, 8))
		.callback(c -> updatePlayerInfoList());

	private final RadioSetting<GrayMode> enlightenGray = new RadioSetting<>(GrayMode.class)
		.name("Grau zu ...")
		.icon(new ItemStack(Blocks.wool, 1, 7))
		.defaultValue(GrayMode.GRAY)
		.callback(c -> updatePlayerInfoList());

	private final RadioSetting<BlackMode> enlightenBlack = new RadioSetting<>(BlackMode.class)
		.name("Schwarz zu ...")
		.icon(new ItemStack(Blocks.wool, 1, 15))
		.defaultValue(BlackMode.BLACK)
		.callback(c -> updatePlayerInfoList());

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Aufhellen")
		.description("Hellt dunkle Texte auf.")
		.icon("light_bulb")
		.callback(c -> updatePlayerInfoList())
		.subSettings(enlightenLightGray, enlightenGray, enlightenBlack,
			new HeaderSetting("§r").scale(.4).entryHeight(10),
			chat, tab, item);

	@Override
	void modify(IChatComponent component) {
		// Enlighten IChatComponent
		if (!component.getUnformattedTextForChat().equals("\u2503 ")) {
			if (component.getChatStyle().getColor() == BLACK)
				component.setChatStyle(component.getChatStyle().setColor(enlightenBlack.get().formatting));
			else if (component.getChatStyle().getColor() == DARK_GRAY)
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

	private enum GrayMode {

		WHITE("Weiß", EnumChatFormatting.WHITE), LIGHT("Hellgrau", EnumChatFormatting.GRAY), GRAY("Grau", DARK_GRAY);

		private final String name;
		private final EnumChatFormatting formatting;

		GrayMode(String name, EnumChatFormatting formatting) {
			this.name = name;
			this.formatting = formatting;
		}

	}

	private enum BlackMode {

		WHITE("Weiß", EnumChatFormatting.WHITE), LIGHT("Hellgrau", EnumChatFormatting.GRAY), GRAY("Grau", DARK_GRAY), BLACK("Schwarz", EnumChatFormatting.BLACK);

		private final String name;
		private final EnumChatFormatting formatting;

		BlackMode(String name, EnumChatFormatting formatting) {
			this.name = name;
			this.formatting = formatting;
		}

	}

}
