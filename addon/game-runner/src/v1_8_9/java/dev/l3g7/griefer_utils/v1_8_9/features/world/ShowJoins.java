/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.ChatMessageLogEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.features.player.player_list.PlayerList;
import dev.l3g7.griefer_utils.v1_8_9.features.player.player_list.ScammerList;
import dev.l3g7.griefer_utils.v1_8_9.features.player.player_list.TrustedList;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import java.util.UUID;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.name;

/**
 * Displays a message when players join or leave.
 */
@Singleton
public class ShowJoins extends Feature {

//TODO:	private final PlayerListSetting players = new PlayerListSetting();

	private final SwitchSetting filter = SwitchSetting.create()
		.name("Joins filtern")
		.description("Ob nur die Joins von bestimmten Spielern angezeigt werden sollen.")
		.icon(Blocks.hopper);

	private final SwitchSetting showOnJoin = SwitchSetting.create()
		.name("Joins beim Betreten des Servers anzeigen")
		.description("Ob beim initialen Laden der Spieler beim Betreten eines Servers eine Join-Nachricht angezeigt werden soll.")
		.icon(Blocks.lever);

	private final SwitchSetting log = SwitchSetting.create()
		.name("Joins im Log speichern")
		.description("Ob die Join-Nachrichten im Log gespeichert werden sollen.")
		.defaultValue(true)
		.icon(Items.writable_book);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Joins anzeigen")
		.description("Zeigt dir an, wenn (bestimmte) Spieler den Server betreten / verlassen.")
		.icon("radar")
		.subSettings(showOnJoin, log, filter,
			HeaderSetting.create().scale(.4).entryHeight(10),
			HeaderSetting.create("§e§lSpieler").scale(.7)
		//TODO:	players
		);

//TODO:	{ players.setContainer(enabled); }

	private boolean onServer = false;

	@EventListener
	private void onServerSwitch(ServerSwitchEvent event) {
		onServer = false;
	}

	@EventListener
	private void onCitybuildJoin(CitybuildJoinEvent event) {
		onServer = true;
	}

	private boolean shouldShowJoin(String name) {
		if (name().equals(name)) // Don't show Joins/Leaves for yourself
			return false;

/*		if(filter.get())
TODO			return players.contains(name, null);*/

		return true;
	}

	@EventListener
	private void onJoin(TabListPlayerAddEvent event) {
		if (!onServer && !showOnJoin.get())
			return;

		String name = event.data.getProfile().getName();
		if (shouldShowJoin(name))
			TickScheduler.runAfterClientTicks(() -> display(Constants.ADDON_PREFIX + "§8[§a+§8] "
				+ getPlayerListPrefix(name, event.data.getProfile().getId())
				+ "§r" + name), 1);
	}

	@EventListener
	private void onQuit(TabListPlayerRemoveEvent event) {
		if (!onServer && !showOnJoin.get())
			return;

		String name = event.cachedName;
		if (shouldShowJoin(name))
			TickScheduler.runAfterClientTicks(() -> display(Constants.ADDON_PREFIX + "§8[§c-§8] "
				+ getPlayerListPrefix(name, event.data.getProfile().getId())
				+ "§r" + name), 1);
	}

	@EventListener
	public void onChatLogModify(ChatMessageLogEvent event) {
		if (!log.get() && !event.message.contains("\u2503") && (event.message.contains("[GrieferUtils] [+] ") || event.message.contains("[GrieferUtils] [-] ")))
			event.cancel();
	}

	private String getPlayerListPrefix(String name, UUID uuid) {
		StringBuilder s = new StringBuilder();

		PlayerList scammerList = FileProvider.getSingleton(ScammerList.class);
		if (scammerList.isEnabled() && scammerList.shouldMark(name, uuid))
			s.append(scammerList.toComponent(scammerList.chatAction.get()).getFormattedText());

		PlayerList trustedList = FileProvider.getSingleton(TrustedList.class);
		if (trustedList.isEnabled() && trustedList.shouldMark(name, uuid))
			s.append(trustedList.toComponent(trustedList.chatAction.get()).getFormattedText());

		return s.toString();
	}

}
