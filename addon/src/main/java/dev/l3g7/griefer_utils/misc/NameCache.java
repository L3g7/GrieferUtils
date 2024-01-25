/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameCache {

	private static final Map<UUID, String> uuidToUser = new HashMap<>();
	private static final Map<String, UUID> nickToUuidCache = new HashMap<>();

	public static String getName(UUID uuid) {
		return uuidToUser.get(uuid);
	}

	public static String getName(String nick) {
		return getName(getUUID(nick));
	}

	public static String ensureRealName(String nick) {
		return nick.contains("~") ? getName(nick) : nick;
	}

	public static UUID getUUID(String nick) {
		return nickToUuidCache.get(nick);
	}

	@EventListener(priority = Priority.HIGH)
	public static void onPacket(PacketEvent.PacketReceiveEvent<S38PacketPlayerListItem> event) {
		switch (event.packet.getAction()) {
			case ADD_PLAYER:
				processAddPacket(event.packet);
				break;
			case UPDATE_DISPLAY_NAME:
				processUpdatePacket(event.packet);
				break;
			case REMOVE_PLAYER:
				processRemovePacket(event.packet);
				break;
		}
	}

	private static void processAddPacket(S38PacketPlayerListItem packet) {
		for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
			if (data.getProfile().getName() != null)
				uuidToUser.put(data.getProfile().getId(), data.getProfile().getName());

			checkForNick(data);
		}
	}

	private static void processUpdatePacket(S38PacketPlayerListItem packet) {
		packet.getEntries().forEach(NameCache::checkForNick);
	}

	private static void processRemovePacket(S38PacketPlayerListItem packet) {
		for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {

			UUID uuid = data.getProfile().getId();

			if (nickToUuidCache.containsValue(data.getProfile().getId()))
				nickToUuidCache.values().removeIf(uuid::equals);

			uuidToUser.remove(uuid);
		}
	}

	private static void checkForNick(S38PacketPlayerListItem.AddPlayerData data) {
		if (data.getDisplayName() == null)
			return;

		String name = data.getDisplayName().getUnformattedText();
		if (name.contains("~"))
			nickToUuidCache.put(name.substring(name.indexOf('~')), data.getProfile().getId());
	}
}