/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.lang.reflect.Array;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ColorPreview extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Farb-Vorschau")
		.description("Zeigt eine gefärbte Vorschau des im Chat eingegebenen Textes an.")
		.icon("labymod:settings/settings/tabping_colored");

	@EventListener
	public void onRender(GuiScreenEvent.DrawScreenEvent event) {
		if (event.gui == null || !(event.gui.getClass() == GuiChat.class || event.gui.getClass() == GuiChatCustom.class))
			return;

		GuiChat gui = (GuiChat) event.gui;
		int buttonWidth = 0;
		if (gui instanceof GuiChatCustom) {
			Object chatButtons = Reflection.get(gui, "chatButtons");

			if (chatButtons != null)
				buttonWidth = Array.getLength(chatButtons) * 14;
		}
		GuiTextField field = Reflection.get(gui, "inputField");
		if (field == null)
			return;

		String text = field.getText();

		// Check if color is present
		if (ModColor.createColors(text).equals(text))
			return;

		int offset = Reflection.get(field, "lineScrollOffset");
		// The text that is currently visible
		String currentText = mc().fontRendererObj.trimStringToWidth(field.getText().substring(offset), field.getWidth());
		// All the color codes that were before it
		String previousColors = field.getText().substring(0, offset).replaceAll("(?<!&)[^&]*", "");

		GuiScreen.drawRect(2, gui.height - 27, gui.width - 2 - buttonWidth, gui.height - 15, Integer.MIN_VALUE);
		mc().fontRendererObj.drawStringWithShadow(ModColor.createColors("§r" + previousColors + currentText), 4, gui.height - 25, 0xFFFFFF);
	}

}
