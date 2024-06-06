/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.UserSetGroupEvent;
import dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits;
import dev.l3g7.griefer_utils.core.misc.badges.BadgeManagerBridge;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.api.Laby;
import net.labymod.api.user.group.Group;
import net.labymod.core.main.user.DefaultGameUser;
import net.labymod.core.main.user.group.GroupHolder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4BadgeManagerBridge implements BadgeManagerBridge {

	private static final Map<UUID, Group> users = new ConcurrentHashMap<>();

	private static long lastRequest = 0;
	private static final Map<UUID, GrieferUtilsGroup> specialBadges = new HashMap<>();
	private static final Set<UUID> queuedUsers = new ConcurrentSet<>();

	public boolean isSpecial(String uuid) {
		return specialBadges.containsKey(UUID.fromString(uuid));
	}

	public void queueUser(UUID uuid) {
		queuedUsers.add(uuid);
		Group group = users.remove(uuid);

		if (group != null)
			setGroup(user(uuid), group);
	}

	public void removeUser(UUID uuid) {
		Group group = users.remove(uuid);

		if (group != null)
			setGroup(user(uuid), group);
	}

	public void clearUsers() {
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
		DefaultGameUser user = (DefaultGameUser) event.user;
		if (!users.containsKey(user.getUniqueId()) || event.group instanceof GrieferUtilsGroup)
			return;

		if (event.group == null)
			users.remove(user.getUniqueId());
		else
			users.put(user.getUniqueId(), (Group) event.group);

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
		event.data.specialBadges.forEach((k, v) -> specialBadges.put(k, new GrieferUtilsGroup(v)));

		Credits.addTeam();
	}

	private static void setGroup(GroupHolder groupHolder, Group group) {
		Reflection.set(groupHolder, "visibleGroup", group);
		Credits.addTeam();
	}

}
