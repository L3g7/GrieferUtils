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

package dev.l3g7.griefer_utils.misc.matrix;

import com.mojang.util.UUIDTypeAdapter;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.matrix.Matrix;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.LibOlmLoader;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.LogoutRequest;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection;
import net.minecraft.util.Session;

import java.io.IOException;

import static dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection.State.CONNECTED;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class MatrixClient extends Matrix {

	private MatrixClient() throws IOException, ReflectiveOperationException {
		authorize();
	}

	public static MatrixClient get() {
		return FileProvider.getSingleton(MatrixClient.class);
	}

	public static boolean isAvailable() {
		return LibOlmLoader.getPath() != null;
	}

	@EventListener
	public void onAccountSwitch(AccountSwitchEvent event) throws IOException {
		if (isAvailable())
			authorize();
	}

	public void authorize() throws IOException {
		Session mcSession = mc().getSession();

		// Check if session is valid
		if (mcSession.getUsername().equals(mcSession.getPlayerID()))
			return;

		// Logout old session
		if (session.authData != null)
			new LogoutRequest().send(session);

		// Login with new session
		authorize("minecraft-" + UUIDTypeAdapter.fromString(mcSession.getPlayerID()), mcSession.getToken());
		updateUsingWhoami();
		setDisplayName(mcSession.getUsername());
		sync();
	}

}