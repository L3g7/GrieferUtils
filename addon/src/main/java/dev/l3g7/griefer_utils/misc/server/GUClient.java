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

package dev.l3g7.griefer_utils.misc.server;

import com.mojang.util.UUIDTypeAdapter;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.server.requests.OnlineUsersRequest;
import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;
import dev.l3g7.griefer_utils.event.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.features.uncategorized.BugReporter;
import net.minecraft.util.Session;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class GUClient {

	private final GUSession session = new GUSession("https://s1.grieferutils.l3g7.dev");

	private GUClient() {
		new Thread(() -> {
			try {
				authorize();
			} catch (IOException | GeneralSecurityException e) {
				BugReporter.reportError(e);
			}
		}).start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (session.isValid())
					session.logout();
			} catch (Throwable ignored) {}
		}));
	}

	public static GUClient get() {
		return FileProvider.getSingleton(GUClient.class);
	}

	@EventListener
	private void onAccountSwitch(AccountSwitchEvent event) {
		new Thread(() -> {
			try {
				authorize();
			} catch (IOException | GeneralSecurityException e) {
				BugReporter.reportError(e);
			}
		}).start();
	}

	public boolean isAvailable() {
		return session.isValid();
	}

	public void authorize() throws IOException, GeneralSecurityException {
		Session mcSession = mc().getSession();

		// Check if session is valid
		if (mcSession.getUsername().equals(mcSession.getPlayerID()))
			return;

		if (session.isValid())
			session.logout();

		// Login with new session
		session.login(UUIDTypeAdapter.fromString(mcSession.getPlayerID()), mcSession.getToken());
	}

	public List<UUID> getOnlineUsers(Set<UUID> requestedUsers) throws IOException {
		List<UUID> users = new OnlineUsersRequest(requestedUsers).send(session);
		if (users == null)
			users = Collections.emptyList();

		return users;
	}

}