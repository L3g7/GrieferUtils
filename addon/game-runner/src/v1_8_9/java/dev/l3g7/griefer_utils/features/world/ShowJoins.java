/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.ChatMessageLogEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby3;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby4;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.player.player_list.PlayerList;
import dev.l3g7.griefer_utils.features.player.player_list.ScammerList;
import dev.l3g7.griefer_utils.features.player.player_list.TrustedList;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;

/**
 * Displays a message when players join or leave.
 */
@Singleton
public class ShowJoins extends Feature {

	private static final Map<UUID, Long> addTimestamps = new HashMap<>();
	private static final Set<UUID> missingGameMode = new HashSet<>();

	private final AbstractSetting<?, List<PlayerListEntry>> players = temp();

	private static AbstractSetting<?, List<PlayerListEntry>> temp() { // TODO refactor
		if (LABY_4.isActive())
			return new PlayerListSettingLaby4()
				.name("Spieler")
				.icon("magnifying_glass");
		else
			return new PlayerListSettingLaby3()
				.name("Spieler")
				.icon("magnifying_glass");
	}

	private final SwitchSetting filter = SwitchSetting.create()
		.name("Joins filtern")
		.description("Ob nur die Joins von bestimmten Spielern angezeigt werden sollen.")
		.icon("players")
		.callback(players::enabled);
	{ players.enabled(filter.get()); }

	private final SwitchSetting showOnJoin = SwitchSetting.create()
		.name("Joins beim Betreten des Servers anzeigen")
		.description("Ob beim initialen Laden der Spieler beim Betreten eines Servers eine Join-Nachricht angezeigt werden soll.")
		.icon("portal");

	private final SwitchSetting log = SwitchSetting.create()
		.name("Joins im Log speichern")
		.description("Ob die Join-Nachrichten im Log gespeichert werden sollen.")
		.defaultValue(true)
		.icon("book_and_quill");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Joins anzeigen")
		.description("Zeigt dir an, wenn (bestimmte) Spieler den Server betreten / verlassen.")
		.icon("players")
		.subSettings(showOnJoin, log, filter, players);

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

		if(!filter.get())
			return true;

		if (name == null)
			return false;

		for (PlayerListEntry entry : players.get())
			if (name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}

	@EventListener
	private void onJoin(TabListPlayerAddEvent event) {
		if (!onServer) {
			if (!showOnJoin.get())
				return;
		} else if (event.data.getPing() != 0) {
			return; // onJoins and /d both have a ping != 0
		}

		String name = event.data.getProfile().getName();
		if (!shouldShowJoin(name))
			return;

		UUID uuid = event.data.getProfile().getId();
		addTimestamps.put(uuid, event.readTime);

		TickScheduler.runAfterClientTicks(() -> {
			if (!addTimestamps.containsKey(uuid))
				return;

			display(Constants.ADDON_PREFIX + "§8[§a+§8] "
				+ getPlayerListPrefix(name, event.data.getProfile().getId())
				+ "§r" + name);
		}, 3);
	}

	@EventListener
	private void onQuit(TabListPlayerRemoveEvent event) {
		if (!onServer && !showOnJoin.get())
			return;

		String name = event.cachedName;
		if (!shouldShowJoin(name))
			return;

		UUID uuid = event.data.getProfile().getId();
		if (onServer)
			missingGameMode.add(uuid);

		TickScheduler.runAfterClientTicks(() -> {
			if (missingGameMode.remove(uuid))
				return;

			long time = event.readTime - (addTimestamps.containsKey(uuid) ? addTimestamps.remove(uuid) : 0);
			if (time < 75_000_000)
				return;

			display(Constants.ADDON_PREFIX + "§8[§c-§8] "
				+ getPlayerListPrefix(name, event.data.getProfile().getId())
				+ "§r" + name);
		}, 3);
	}

	@EventListener
	private void onUpdateGamemode(PacketReceiveEvent<S38PacketPlayerListItem> event) {
		if (event.packet.getAction() != S38PacketPlayerListItem.Action.UPDATE_GAME_MODE)
			return;

		for (S38PacketPlayerListItem.AddPlayerData entry : event.packet.getEntries())
			missingGameMode.remove(entry.getProfile().getId());
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
