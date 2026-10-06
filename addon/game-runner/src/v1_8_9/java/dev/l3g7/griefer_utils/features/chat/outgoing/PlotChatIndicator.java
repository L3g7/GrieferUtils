/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.network.GrieferGamesPayloadEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;

import java.io.DataInputStream;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * Draws an orange frame around the chat input box if plot chat is activated.
 */
@Singleton
public class PlotChatIndicator extends Feature {

	private Boolean plotchatState = null;

	private final SwitchSetting replaceGlobalChat = SwitchSetting.create()
		.name("@ ersetzen")
		.description("Ersetzt @ mit /globalchat, wenn der Plot-Chat aktiviert ist.")
		.icon("chat")
		.since("2.4-BETA-1");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Plot-Chat-Indikator")
		.description("Zeichnet einen orangen Rahmen um die Chateingabe, wenn der Plotchat aktiviert ist.")
		.icon("chat_orange")
		.subSettings(replaceGlobalChat);

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent event) {
		plotchatState = null;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onPlotChatConfiguration(GrieferGamesPayloadEvent event) {
		if (!event.channel.equals("plotchat_configuration"))
			return;

		try (DataInputStream in = event.createStream()) {
			plotchatState = in.readBoolean();
		} catch (IOException e) {
			throw elevate(e);
		}
	}

	@EventListener(priority = Priority.HIGH)
	private void replaceGlobalChat(MessageSendEvent event) {
		if (!replaceGlobalChat.get())
			return;

		if (plotchatState != null && plotchatState && event.message.startsWith("@")) {
			event.cancel();
			String newMessage = "/globalchat " + event.message.substring(1);
			if (!MessageEvent.MessageSendEvent.post(newMessage))
				player().sendChatMessage(newMessage);
		}
	}

	@EventListener
	public void onRender(DrawScreenEvent event) {
		if (plotchatState == null || !plotchatState)
			return;

		GuiScreen gui = event.gui;
		if (!(gui instanceof GuiChat))
			return;

		int buttonWidth = labyBridge.chatButtonWidth();
		int color = 0xFFFFA126;

		// Render frame
		GuiScreen.drawRect(1, gui.height - 15, gui.width - 1 - buttonWidth, gui.height - 14, color);
		GuiScreen.drawRect(1, gui.height - 2, gui.width - 1 - buttonWidth, gui.height - 1, color);
		GuiScreen.drawRect(1, gui.height - 15, 2, gui.height - 1, color);
		GuiScreen.drawRect(gui.width - 2 - buttonWidth, gui.height - 15, gui.width - 1 - buttonWidth, gui.height - 1, color);
	}

}