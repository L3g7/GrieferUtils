/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class ColorPreview extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Farb-Vorschau")
		.description("Zeigt eine gefärbte Vorschau des im Chat eingegebenen Textes an.")
		.icon("labymod_3/tabping_colored");

	@EventListener
	public void onRender(GuiScreenEvent.DrawScreenEvent event) {
		GuiScreen gui = event.gui;
		if (!(gui instanceof GuiChat))
			return;

		int buttonWidth = labyBridge.chatButtonWidth();
		GuiTextField field = Reflection.get(gui, "inputField");
		if (field == null)
			return;

		String text = field.getText();

		// Check if color is present
		if (createColors(text).equals(text))
			return;

		int offset = Reflection.get(field, "lineScrollOffset");
		// The text that is currently visible
		String currentText = mc().fontRendererObj.trimStringToWidth(field.getText().substring(offset), field.getWidth());
		// All the color codes that were before it
		String previousColors = field.getText().substring(0, offset).replaceAll("(?<!&)[^&]*", "");

		GuiScreen.drawRect(2, gui.height - 27, gui.width - 2 - buttonWidth, gui.height - 15, Integer.MIN_VALUE);
		mc().fontRendererObj.drawStringWithShadow(createColors("§r" + previousColors + currentText), 4, gui.height - 25, 0xFFFFFF);
	}

	private static String createColors(String text) {
		return text.replaceAll("(?i)&([a-z0-9])", "§$1");
	}

}
