/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Draws an orange frame around the chat input box if plot chat is activated.
 */
@Singleton
public class PlotChatIndicator extends Feature {

	private final List<Citybuild> specialServers = ImmutableList.of(NATURE, EXTREME, CBE, EVENT);
	private StringBuilder states = new StringBuilder(Strings.repeat("?", 27)); // A StringBuilder is used since it has .setCharAt, and with HashMaps you'd have 26 entries per account in the config)

	private Boolean plotchatState = null;
	private boolean waitingForPlotchatStatus = false;

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
		.subSettings(replaceGlobalChat)
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
		String path = "chat.outgoing.plot_chat_indicator.states." + mc().getSession().getProfile().getId();
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
		Citybuild citybuild = getCurrentCitybuild();
		if (citybuild == ANY || citybuild == LAVA || citybuild == WATER || citybuild == MAGIC_FOREST) {
			plotchatState = false;
			return;
		}

		char character = states.charAt(getIndex(citybuild));
		plotchatState = character == '?' ? null : character == 'Y';

		if (plotchatState != null || !isEnabled())
			return;

		waitingForPlotchatStatus = true;
		send("/p chat");
	}

	@EventListener(triggerWhenDisabled = true)
	public void onReceive(MessageReceiveEvent event) {
		Citybuild citybuild = getCurrentCitybuild();
		if (citybuild == ANY || citybuild == MAGIC_FOREST)
			return;

		// Update plot chat state
		if (event.message.getFormattedText().matches("^§r§8\\[§r§6GrieferGames§r§8] §r§.Die Einstellung §r§.chat §r§.wurde (?:de)?aktiviert\\.§r$")) {
			plotchatState = event.message.getFormattedText().contains(" aktiviert");
			states.setCharAt(getIndex(citybuild), plotchatState ? 'Y' : 'N');
			Config.set("chat.outgoing.plot_chat_indicator.states." + mc().getSession().getProfile().getId(), new JsonPrimitive(states.toString()));
			Config.save();

			if (waitingForPlotchatStatus) {
				waitingForPlotchatStatus = false;
				send("/p chat");
			}
		}
	}

	private int getIndex(Citybuild server) {
		if (specialServers.contains(server))
			return specialServers.indexOf(server) + 22;

		return server.ordinal();
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