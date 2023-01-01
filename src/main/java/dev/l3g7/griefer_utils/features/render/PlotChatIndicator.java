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

package dev.l3g7.griefer_utils.features.render;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.lang.reflect.Array;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.util.misc.ServerCheck.isOnGrieferGames;

/**
 * Draws an orange frame around the chat input box if plot chat is activated.
 */
@Singleton
public class PlotChatIndicator extends Feature {


	private final List<String> specialServers = ImmutableList.of("Nature", "Extreme", "CBE", "Event");
	private StringBuilder states; // A StringBuilder is used since it has .setChatAt, and with HashMaps you'd have 26 entries per account in the config)
	private String server;

	private Boolean plotchatState = null;
	private boolean waitingForPlotchatStatus = false;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Plot-Chat-Indikator")
		.description("Zeichnet einen orangen Rahmen um die Chateingabe, wenn der Plotchat aktiviert ist.")
		.icon("speech_bubble")
		.callback(enabled -> {
			if (enabled && isOnCityBuild() && plotchatState == null && !waitingForPlotchatStatus) {
				waitingForPlotchatStatus = true;
				send("/p chat");
			}
		});

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent event) {
		plotchatState = null;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerJoin(ServerEvent.ServerJoinEvent event) {
		if (!isOnGrieferGames())
			return;

		String path = "chat.plot_chat_indicator.states." + mc().getSession().getProfile().getId();
		if (Config.has(path))
			states = new StringBuilder(Config.get(path).getAsString());
		else
			states = new StringBuilder(Strings.repeat("?", 26));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCityBuildJoin(CityBuildJoinEvent event) {
		ScorePlayerTeam team = world().getScoreboard().getTeam("server_value");
		server = team == null ? "" : ModColor.removeColor(team.getColorPrefix());

		if (server.equals("Lava") || server.equals("Wasser")) {
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
	public void onReceive(ClientChatReceivedEvent event) {

		// Update plot chat state
		if (event.message.getFormattedText().matches("^§r§8\\[§r§6GrieferGames§r§8] §r§.Die Einstellung §r§.chat §r§.wurde (?:de)?aktiviert\\.§r$")) {
			plotchatState = event.message.getFormattedText().contains(" aktiviert");
			states.setCharAt(getIndex(server), plotchatState ? 'Y' : 'N');
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
	public void onRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if (plotchatState == null || !plotchatState)
			return;

		GuiScreen gcc = event.gui;
		if (!(gcc instanceof GuiChat))
			return;

		int buttonWidth = gcc instanceof GuiChatCustom ? Array.getLength(Reflection.get(gcc, "chatButtons")) * 14 : 0;
		int color = 0xFFFFA126;

		// Render frame
		GuiScreen.drawRect(2, gcc.height - 14, gcc.width - 2 - buttonWidth, gcc.height - 2, 100 << 24);

		GuiScreen.drawRect(1, gcc.height - 15, gcc.width - 1 - buttonWidth, gcc.height - 14, color);
		GuiScreen.drawRect(1, gcc.height - 2, gcc.width - 1 - buttonWidth, gcc.height - 1, color);
		GuiScreen.drawRect(1, gcc.height - 15, 2, gcc.height - 1, color);
		GuiScreen.drawRect(gcc.width - 2 - buttonWidth, gcc.height - 15, gcc.width - 1 - buttonWidth, gcc.height - 1, color);
	}

}