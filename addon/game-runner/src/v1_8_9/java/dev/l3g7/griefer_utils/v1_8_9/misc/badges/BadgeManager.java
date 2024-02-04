/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.badges;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.UserSetGroupEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.Credits;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.api.Laby;
import net.labymod.api.user.group.Group;
import net.labymod.core.main.user.DefaultGameUser;
import net.labymod.core.main.user.group.GroupHolder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BadgeManager {

	private static final Map<UUID, Group> users = new ConcurrentHashMap<>();

	private static long lastRequest = 0;
	private static Map<UUID, GrieferUtilsGroup> specialBadges = new HashMap<>();
	private static final Set<UUID> queuedUsers = new ConcurrentSet<>();

	public static boolean isSpecial(String uuid) {
		return specialBadges.containsKey(UUID.fromString(uuid));
	}

	public static void queueUser(UUID uuid) {
		queuedUsers.add(uuid);
	}

	public static void removeUser(UUID uuid) {
		setGroup(user(uuid), users.remove(uuid));
	}

	public static void clearUsers() {
		for (UUID uuid : users.keySet())
			removeUser(uuid);
	}

	private static GroupHolder user(UUID uuid) {
		DefaultGameUser user = (DefaultGameUser) Laby.references().gameUserService().gameUser(uuid);
		return user.groupHolder();
	}

	@EventListener
	private static void onTick(TickEvent.ClientTickEvent event) {
		if (lastRequest + 2500 <= System.currentTimeMillis())
			requestQueuedUsers();
	}

	@EventListener
	private static void onSetGroup(UserSetGroupEvent event) {
		if (!users.containsKey(event.user.getUniqueId()) || event.group instanceof GrieferUtilsGroup)
			return;

		if (event.group == null)
			users.remove(event.user.getUniqueId());
		else
			users.put(event.user.getUniqueId(), event.group);

		event.cancel();
	}

	private static void requestQueuedUsers() {
		if (queuedUsers.isEmpty() || !GUClient.get().isAvailable())
			return;

		lastRequest = System.currentTimeMillis();

		Set<UUID> requestedUsers = new ConcurrentSet<>();
		requestedUsers.addAll(queuedUsers);
		queuedUsers.removeAll(requestedUsers);

		CompletableFuture.supplyAsync((Supplier<List<UUID>>) () -> GUClient.get().getOnlineUsers(requestedUsers)).thenAccept(uuids -> {
			for (UUID uuid : uuids) {
				users.put(uuid, user(uuid).visibleGroup());
				setGroup(user(uuid), specialBadges.getOrDefault(uuid, new GrieferUtilsGroup()));
			}
		});
	}

	@EventListener
	private static void onWebData(WebDataReceiveEvent event) {
		specialBadges = event.data.specialBadges;
		specialBadges.put(UUID.fromString("75bbfffc-e53c-41f0-9db7-dd2ec6de3673"), specialBadges.get(UUID.fromString("2b453b54-baec-4c83-b323-b242d341079c")));
		Credits.addTeam();
	}

	private static void setGroup(GroupHolder groupHolder, Group group) {
		Reflection.set(groupHolder, "visibleGroup", group);
	}

}
