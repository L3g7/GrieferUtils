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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.ingamechat.GuiChatCustom;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.lang.reflect.Array;
import java.util.HashMap;

import static dev.l3g7.griefer_utils.features.render.PlotChatIndicator.PlotChatState.*;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * Draws an orange frame around the chat input box if plot chat is activated.
 */
@Singleton
public class PlotChatIndicator extends Feature {

	private final HashMap<String, PlotChatState> serverCache = new HashMap<>();
	private PlotChatState state = UNKNOWN;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Plot-Chat-Indikator")
		.description("Zeichnet einen orangen Rahmen um die Chateingabe, wenn der Plotchat aktiviert ist.")
		.icon("speech_bubble")
		.callback(enabled -> {
			if (enabled && isOnCityBuild() && state == UNKNOWN) {
				state = INITIAL_CHECK;
				send("/p chat");
			}
		});

	@EventListener
	private void onServerSwitch(ServerSwitchEvent event) {
		System.out.println("ServerSwitchEvent");
		state = UNKNOWN;
	}

	@EventListener
	private void onCityBuildJoin(CityBuildJoinEvent event) {
		System.out.println("CityBuildJoinEvent");
		if (state != UNKNOWN)
			return;

		String server = getServerFromScoreboard();

		if (serverCache.containsKey(server))
			state = serverCache.get(server);
		else {
			state = INITIAL_CHECK;
			send("/p chat");
		}
	}

	@EventListener
	private void onMessage(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().matches("^\\[GrieferGames] Die Einstellung chat wurde (?:de)?aktiviert\\.$")) {
			if (state == INITIAL_CHECK)
				send("/p chat");
			state = event.message.getUnformattedText().endsWith(" aktiviert.") ? ACTIVE : INACTIVE;
			serverCache.put(getServerFromScoreboard(), state);
		}
	}

	@EventListener
	private void onRender(RenderGameOverlayEvent.Post event) {
		if (state != ACTIVE)
			return;

		// Check if chat is open
		if (event.type != RenderGameOverlayEvent.ElementType.CHAT)
			return;

		GuiScreen gcc = mc().currentScreen;
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

	enum PlotChatState {
		UNKNOWN, INITIAL_CHECK, ACTIVE, INACTIVE
	}

}
