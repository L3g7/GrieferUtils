/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import dev.l3g7.griefer_utils.core.misc.functions.Function;
import dev.l3g7.griefer_utils.misc.ChatLineUtil;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.path;

public class CopyTextEntry extends ChatMenuEntry {

	protected final String configKey = "chat.chat_menu.entries." + name + ".";

	private final DropDownSetting<CopyFormat> copyFormat = new DropDownSetting<>(CopyFormat.class)
		.name("Format")
		.description("Wie der kopierte Text sein soll.")
		.config(configKey + "format")
		.defaultValue(CopyFormat.UNFORMATTED)
		.icon(Material.PAPER);

	private final BooleanSetting modifiedMessage = new BooleanSetting()
		.name("Bearbeitungen kopieren")
		.description("Ob der Text mit den Bearbeitungen u.a. von GrieferUtils kopiert werden soll.")
		.config(configKey + "modified_message")
		.icon(Material.BOOK_AND_QUILL);

	private final BooleanSetting settingContainer = new BooleanSetting()
		.name(name)
		.subSettings(copyFormat, modifiedMessage);

	private final DisplaySetting mainSetting = (DisplaySetting) new DisplaySetting()
		.name(name)
		.icon(icon)
		.defaultValue(true)
		.config(configKey + "enabled")
		.callback(v -> enabled = v);

	public CopyTextEntry() {
		super("Text kopieren", null, null, "clipboard");
	}

	@Override
	public void trigger(String name, IChatComponent entireText) {
		IChatComponent icc = modifiedMessage.get() ? entireText : ChatLineUtil.getUnmodifiedIChatComponent(entireText);
		ChatMenu.copyToClipboard(copyFormat.get().componentToString.apply(icc));
	}

	public SettingsElement getSetting() {
		return mainSetting;
	}

	private enum CopyFormat {
		UNFORMATTED("Unformattiert", IChatComponent::getUnformattedText),
		FORMATTED("Formattiert", IChatComponent::getFormattedText),
		JSON("JSON", IChatComponent.Serializer::componentToJson);

		final String name;
		final Function<IChatComponent, String> componentToString;

		CopyFormat(String name, Function<IChatComponent, String> componentToString) {
			this.name = name;
			this.componentToString = componentToString;
		}

	}

	private class DisplaySetting extends BooleanSetting {

		private boolean editHovered = false;

		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			if (editHovered) {
				path().add(settingContainer);
				mc.currentScreen.initGui();
			}
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);

			editHovered = mouseX > maxX - 70 && mouseX < maxX - 55 && mouseY > y + 4 && mouseY < y + 20;
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
			drawUtils().drawTexture(maxX - 66 - (editHovered ? 4 : 3), y + (editHovered ? 3.5 : 4.5), 256, 256, editHovered ? 16 : 14, editHovered ? 16 : 14);
		}

	}

}
