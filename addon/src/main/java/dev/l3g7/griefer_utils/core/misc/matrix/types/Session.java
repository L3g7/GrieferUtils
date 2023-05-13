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

package dev.l3g7.griefer_utils.core.misc.matrix.types;

import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmAccount;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.WhoamiRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.keys.KeysUploadRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Session {

	public final String host;

	public String userId;
	public String deviceId;
	public AuthData authData;
	public OlmAccount olmAccount;

	public final Map<String, Room> rooms = new HashMap<>();
	public Map<String, List<String>> directRooms = new HashMap<>();

	public transient Map<String, User> users = new HashMap<>();

	public final KeyStore keyStore = new KeyStore();

	public Session(String host) {
		this.host = host;
	}

	public Session(String host, AuthData authData, OlmAccount olmAccount) {
		this.host = host;
		this.authData = authData;
		this.olmAccount = olmAccount;
	}

	@Deprecated
	public CompletableFuture<Void> updateUsingWhoami() {
		return new WhoamiRequest().sendAsync(this).thenAccept(whoamiResponse -> {
			userId = whoamiResponse.user_id;
			deviceId = whoamiResponse.device_id;
		});
	}

	public void uploadOneTimeKeys(int count) {
		olmAccount.generateOneTimeKeys(count);
		Map<String, Curve25519Keys.SignedCurve25519Key> oneTimeKeys = olmAccount.getOneTimeKeys().sign(this, false);
		olmAccount.markKeysAsPublished();
		new KeysUploadRequest(null, null, oneTimeKeys).sendAsync(this);
		save();
	}

	public void save() {
		try (FileWriter w = new FileWriter("matrix_account.json")) {
			w.write(MatrixUtil.GSON.toJson(this));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}