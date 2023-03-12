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

package dev.l3g7.griefer_utils.features.chat.encrypted_messages;

import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EncryptedSessions {

	private static final Map<UUID, EncryptedSession> UUID_TO_SESSION = new HashMap<>();

	public static void addSession(UUID uuid, byte[] key, byte[] iv) {
		UUID_TO_SESSION.put(uuid, new EncryptedSession(key, iv));
	}

	public static boolean sessionExists(UUID uuid) {
		return UUID_TO_SESSION.containsKey(uuid);
	}

	public static String encrypt(UUID uuid, String message) {
		return UUID_TO_SESSION.get(uuid).encrypt(message);
	}

	public static String decrypt(UUID uuid, String message) {
		return UUID_TO_SESSION.get(uuid).decrypt(message);
	}

	@EventListener
	private static void onServerLeave(ServerEvent.ServerQuitEvent event) {
		UUID_TO_SESSION.clear();
	}

	@EventListener
	private static void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		UUID_TO_SESSION.clear();
	}

	@EventListener
	private static void onPlayerLeave(TabListEvent.TabListPlayerRemoveEvent event) {
		UUID_TO_SESSION.remove(event.data.getProfile().getId());
	}

	private static class EncryptedSession {

		private final Cipher encrypt;
		private final Cipher decrypt;

		private EncryptedSession(byte[] key, byte[] iv) {
			this(new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
		}

		private EncryptedSession(SecretKey key, IvParameterSpec iv) {
			try {
				encrypt = Cipher.getInstance("AES/CBC/NoPadding");
				decrypt = Cipher.getInstance("AES/CBC/NoPadding");
				encrypt.init(Cipher.ENCRYPT_MODE, key, iv);
				decrypt.init(Cipher.DECRYPT_MODE, key, iv);
			} catch (GeneralSecurityException e) {
				throw Util.elevate(e);
			}
		}

		private String encrypt(String payload) {
			if (payload.length() > 48)
				throw new IllegalArgumentException("Can't encrypt payloads larger than 48 bytes! Payload was " + payload);

			byte[] bytes = new byte[48];
			Arrays.fill(bytes, (byte) 0);
			System.arraycopy(payload.getBytes(), 0, bytes, 0, payload.length());

			return Base100.encode(encrypt.update(bytes));
		}

		private String decrypt(String payload) {
			return new String(decrypt.update(Base100.decode(payload))).replace("\0", "");
		}

	}

}
