/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.server;

import com.mojang.util.UUIDTypeAdapter;
import dev.l3g7.griefer_utils.api.BugReporter;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.api.misc.server.requests.OnlineUsersRequest;
import dev.l3g7.griefer_utils.api.misc.server.requests.hive_mind.BoosterRequest;
import dev.l3g7.griefer_utils.api.misc.server.requests.hive_mind.MobRemoverRequest;
import dev.l3g7.griefer_utils.api.misc.server.types.GUSession;
import net.minecraft.util.Session;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import static dev.l3g7.griefer_utils.api.event.event_bus.Priority.HIGH;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class GUClient {

	private final GUSession session = new GUSession("https://s1.grieferutils.l3g7.dev");

	private GUClient() {
		new Thread(this::authorize).start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (session.isValid())
					session.logout();
			} catch (Throwable ignored) {}
		}));
	}

	@OnEnable
	public static GUClient get() {
		return FileProvider.getSingleton(GUClient.class);
	}

	@EventListener(priority = HIGH)
	private void onAccountSwitch(AccountSwitchEvent event) {
		new Thread(this::authorize).start();
	}

	public boolean isAvailable() {
		return session.isValid();
	}

	public void authorize() {
		Session mcSession = mc().getSession();

		// Check if session is valid
		if (mcSession.getUsername().equals(mcSession.getPlayerID()))
			return;

		if (session.isValid())
			session.logout();

		// Login with new session
		try {
			session.login(UUIDTypeAdapter.fromString(mcSession.getPlayerID()), mcSession.getToken());
		} catch (GeneralSecurityException e) {
			BugReporter.reportError(e);
		}
	}

	public List<UUID> getOnlineUsers(Set<UUID> requestedUsers) {
		List<UUID> users = new OnlineUsersRequest(requestedUsers).send(session);
		if (users == null)
			users = Collections.emptyList();

		return users;
	}

	public void sendMobRemoverData(Citybuild citybuild, Long value) {
		new MobRemoverRequest(citybuild.getInternalName(), value).send(session);
	}

	public Long getMobRemoverData(Citybuild citybuild) {
		return new MobRemoverRequest(citybuild.getInternalName(), null).send(session);
	}

	public void sendBoosterData(Citybuild citybuild, Map<String, List<Long>> value) {
		new BoosterRequest(citybuild.getInternalName(), value).send(session);
	}

	public Map<String, List<Long>> getBoosterData(Citybuild citybuild, Consumer<IOException> errorHandler) {
		return new BoosterRequest(citybuild, 1000L).request(session, errorHandler, true);
	}

	public LeaderboardData getLeaderboardData() {
		return new LeaderboardRequest(false).get(session);
	}

	public LeaderboardData sendLeaderboardData(boolean flown) {
		return new LeaderboardRequest(flown).send(session);
	}

}