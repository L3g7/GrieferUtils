/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby3;

import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import net.minecraft.init.Items;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class CopyTextEntry extends ChatMenuEntry {

	protected final String configKey = "chat.chat_menu.entries." + name + ".";

	private final DropDownSetting<CopyFormat> copyFormat = DropDownSetting.create(CopyFormat.class)
		.name("Format")
		.description("Wie der kopierte Text sein soll.")
		.config(configKey + "format")
		.defaultValue(CopyFormat.UNFORMATTED)
		.icon(Items.paper);

	private final SwitchSetting modifiedMessage = SwitchSetting.create()
		.name("Bearbeitungen kopieren")
		.description("Ob der Text mit den Bearbeitungen u.a. von GrieferUtils kopiert werden soll.")
		.config(configKey + "modified_message")
		.icon(Items.writable_book);

	private final SwitchSetting settingContainer = SwitchSetting.create()
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
	public void trigger(String name, IChatComponent modifiedText, IChatComponent unmodifiedText) {
		IChatComponent icc = modifiedMessage.get() ? modifiedText : unmodifiedText;
		ChatMenu.copyToClipboard(copyFormat.get().componentToString.apply(icc));
	}

	public BaseSetting<?> getSetting() {
		return mainSetting;
	}

	private enum CopyFormat implements Named {
		UNFORMATTED("Unformattiert", icc -> icc.getUnformattedText().replaceAll("§.", "")),
		FORMATTED("Formattiert", IChatComponent::getFormattedText),
		JSON("JSON", IChatComponent.Serializer::componentToJson);

		final String name;
		final Function<IChatComponent, String> componentToString;

		CopyFormat(String name, Function<IChatComponent, String> componentToString) {
			this.name = name;
			this.componentToString = componentToString;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	private class DisplaySetting extends SwitchSettingImpl {

		private boolean editHovered = false;

		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			if (editHovered) {
				Reflection.<ArrayList<BaseSetting<?>>>get(mc.currentScreen, "path").add(settingContainer);
				mc.currentScreen.initGui();
			}
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);

			editHovered = mouseX > maxX - 70 && mouseX < maxX - 55 && mouseY > y + 4 && mouseY < y + 20;
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
			DrawUtils.drawTexture(maxX - 66 - (editHovered ? 4 : 3), y + (editHovered ? 3.5 : 4.5), 256, 256, editHovered ? 16 : 14, editHovered ? 16 : 14);
		}

	}

}
