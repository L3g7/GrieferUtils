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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.PlayerListSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.PlayerDataProvider;
import dev.l3g7.griefer_utils.util.misc.TickScheduler;
import net.labymod.utils.Material;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.name;

/**
 * Displays a message when players join or leave.
 */
@Singleton
public class ShowJoins extends Feature {

	private final PlayerListSetting players = new PlayerListSetting();

	private final BooleanSetting filter = new BooleanSetting()
		.name("Joins filtern")
		.description("Ob nur die Joins von bestimmten Spielern angezeigt werden sollen.")
		.icon(Material.HOPPER);

	private final BooleanSetting showOnJoin = new BooleanSetting()
		.name("Joins beim Betreten des Servers anzeigen")
		.icon(Material.LEVER);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Joins anzeigen")
		.description("Zeigt dir an, wenn (bestimmte) Spieler den Server betreten / verlassen.")
		.icon("radar")
		.subSettings(showOnJoin, filter,
			new HeaderSetting("§r").scale(.4).entryHeight(10),
			new HeaderSetting("§e§lSpieler").scale(.7),
			players);

	{ players.setContainer(enabled); }

	private boolean onServer = false;

	@EventListener
	private void onServerSwitch(ServerSwitchEvent event) {
		onServer = false;
	}

	@EventListener
	private void onCityBuildJoin(CityBuildJoinEvent event) {
		onServer = true;
	}

	private boolean shouldShowJoin(String name) {
		if (name().equals(name)) // Don't show Joins/Leaves for yourself
			return false;

		if(filter.get())
			return players.get().contains(PlayerDataProvider.get(name).getUuid());

		return true;
	}

	@EventListener
	private void onJoin(TabListPlayerAddEvent event) {
		if (!onServer && !showOnJoin.get())
			return;

		String name = event.data.getProfile().getName();
		if (shouldShowJoin(name))
			TickScheduler.runAfterClientTicks(() -> display(Constants.ADDON_PREFIX + "§8[§a+§8] §r" + name), 1);
	}

	@EventListener
	private void onQuit(TabListPlayerRemoveEvent event) {
		if (!onServer && !showOnJoin.get())
			return;

		String name = event.cachedName;
		if (shouldShowJoin(name))
			TickScheduler.runAfterClientTicks(() -> display(Constants.ADDON_PREFIX + "§8[§c-§8] §r" + name), 1);
	}

}
