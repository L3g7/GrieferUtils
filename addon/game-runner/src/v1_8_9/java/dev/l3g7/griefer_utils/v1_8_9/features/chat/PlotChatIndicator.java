/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

/**
 * Draws an orange frame around the chat input box if plot chat is activated.
 */
@Singleton
public class PlotChatIndicator extends Feature {

	private final List<String> specialServers = ImmutableList.of("Nature", "Extreme", "CBE", "Event", "CBT");
	private StringBuilder states = new StringBuilder(Strings.repeat("?", 27)); // A StringBuilder is used since it has .setCharAt, and with HashMaps you'd have 26 entries per account in the config)

	private Boolean plotchatState = null;
	private boolean waitingForPlotchatStatus = false;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Plot-Chat-Indikator")
		.description("Zeichnet einen orangen Rahmen um die Chateingabe, wenn der Plotchat aktiviert ist.")
		.icon("speech_bubble")
		.callback(enabled -> {
			if (enabled && ServerCheck.isOnCitybuild() && plotchatState == null && !waitingForPlotchatStatus) {
				waitingForPlotchatStatus = true;
				send("/p chat");
			}
		});

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent event) {
		plotchatState = null;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerJoin(GrieferGamesJoinEvent event) {
		String path = "chat.plot_chat_indicator.states." + mc().getSession().getProfile().getId();
		if (Config.has(path)) {
			try {
				states = new StringBuilder(Config.get(path).getAsString());
				if (states.length() == 26)
					states.append('?');
				return;
			} catch (UnsupportedOperationException ignored) {
				// Fix for old configs
			}
		}

		states = new StringBuilder(Strings.repeat("?", 27));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCitybuildJoin(CitybuildJoinEvent event) {
		String server = getServerFromScoreboard();
		if (server.isEmpty() || server.equals("Lava") || server.equals("Wasser") || server.equals("Portal")) {
			plotchatState = false;
			return;
		}

		char character = states.charAt(getIndex(server));
		plotchatState = character == '?' ? null : character == 'Y';

		if (plotchatState != null || !isEnabled())
			return;

		waitingForPlotchatStatus = true;
		send("/p chat");
	}

	@EventListener(triggerWhenDisabled = true)
	public void onReceive(MessageReceiveEvent event) {
		if (getServerFromScoreboard().isEmpty())
			return;

		// Update plot chat state
		if (event.message.getFormattedText().matches("^§r§8\\[§r§6GrieferGames§r§8] §r§.Die Einstellung §r§.chat §r§.wurde (?:de)?aktiviert\\.§r$")) {
			plotchatState = event.message.getFormattedText().contains(" aktiviert");
			states.setCharAt(getIndex(getServerFromScoreboard()), plotchatState ? 'Y' : 'N');
			Config.set("chat.plot_chat_indicator.states." + mc().getSession().getProfile().getId(), new JsonPrimitive(states.toString()));
			Config.save();

			if (waitingForPlotchatStatus) {
				waitingForPlotchatStatus = false;
				send("/p chat");
			}
		}
	}

	private int getIndex(String server) {
		if (specialServers.contains(server))
			return specialServers.indexOf(server) + 22;

		return Integer.parseInt(server.substring(2)) - 1;
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