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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.features.chat.encrypted_messages.EncryptedMessages.*;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class HandShaker {

	private static final Map<UUID, byte[]> RANDOMS = new HashMap<>();

	private static final Curve25519 CURVE = Curve25519.getInstance(Curve25519.BEST);
	private static final Curve25519KeyPair KEY_PAIR = CURVE.generateKeyPair();

	public static void startHandShake(UUID uuid) {
		new Thread(() -> {
			displayFeedback(true, "Es wird versucht, eine verschlüsselte Verbindung zu " + NameCache.getName(uuid) + " aufzubauen...");

			byte[] random = get16RandomBytes();
			if (!joinServer(random)) {
				displayFeedback(false, "Ein Fehler ist aufgetreten: Deine Echtheit konnte nicht verifiziert werden werden");
				return;
			}

			RANDOMS.put(uuid, random);
			sendHandShakeMessage(uuid, random);
		}).start();
	}

	public static void onHandShakeReceive(String name, String data) {
		UUID uuid = name.contains("~") ? NameCache.getUUID(name) : PlayerUtil.getUUID(name);
		if (uuid == null) {
			fail(null, "Der Empänger konnte nicht gefunden werden!");
			return;
		}

		boolean isStart = !RANDOMS.containsKey(uuid);

		if (isStart)
			displayFeedback(true, name + " versucht eine verschlüsselte Verbindung aufzubauen...");

		// Extract data
		byte[] bytes = Base100.decode(data);
		byte[] customData = new byte[16];
		System.arraycopy(bytes, 32, customData, 0, 16);

		byte[] random = isStart ? customData : RANDOMS.get(uuid);

		// Verify
		if (!hasJoinedServer(NameCache.getName(uuid), random)) {
			fail(uuid, "Die Echtheit des Empfängers konnte nicht verifiziert werden!");
			return;
		}

		if (isStart && !joinServer(random)) {
			fail(uuid, "Deine Echtheit konnte nicht verifiziert werden!");
			return;
		}

		// Create session
		byte[] publicKey = new byte[32];
		System.arraycopy(bytes, 0, publicKey, 0, 32);

		byte[] iv = isStart ? get16RandomBytes() : customData;
		byte[] agreement = CURVE.calculateAgreement(publicKey, KEY_PAIR.getPrivateKey());
		byte[] shortenedKey = new byte[16];
		System.arraycopy(agreement, 0, shortenedKey, 0, 16);
		EncryptedSessions.addSession(uuid, shortenedKey, iv);

		// Response
		if (isStart)
			sendHandShakeMessage(uuid, iv);
		else
			MinecraftForge.EVENT_BUS.post(new MessageEvent.MessageSendEvent(message, false));

		RANDOMS.remove(uuid);
		displayFeedback(true, "Die verschlüsselte Verbindung zu " + NameCache.getName(uuid) + " wurde erfolgreich aufgebaut.");
	}

	private static void sendHandShakeMessage(UUID uuid, byte[] customData) {
		byte[] data = new byte[48];
		System.arraycopy(KEY_PAIR.getPublicKey(), 0, data, 0, 32);
		System.arraycopy(customData, 0, data, 32, 16);

		send("/msg %s %s%s", NameCache.getName(uuid), Base100.encode(data), HANDSHAKE_START_SUFFIX);
	}

	private static boolean joinServer(byte[] random) {
		String serverIp = getServerIP(random);
		try {
			mc().getSessionService().joinServer(player().getGameProfile(), mc().getSession().getToken(), serverIp);
			return true;
		} catch (AuthenticationException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean hasJoinedServer(String name, byte[] random) {
		String serverIp = getServerIP(random);
		try {
			return mc().getSessionService().hasJoinedServer(new GameProfile(null, name), serverIp) != null;
		} catch (AuthenticationUnavailableException e) {
			return false;
		}
	}

	private static String getServerIP(byte[] random) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(random);
			return new BigInteger(bytes).toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw Util.elevate(e);
		}
	}

	private static byte[] get16RandomBytes() {
		byte[] bytes = new byte[16];
		new SecureRandom().nextBytes(bytes);
		return bytes;
	}

	private static void displayFeedback(boolean successful, String message) {
		String color = successful ? "§a" : "§c";
		message = message == null ? "" : color + message;

		player().addChatMessage(new ChatComponentText(Constants.ADDON_PREFIX + (successful ? LOCK : BROKEN_LOCK) + message));
	}

	private static void fail(UUID uuid, String message) {
		RANDOMS.remove(uuid);
		displayFeedback(false, "Ein Fehler ist aufgetreten: " + message);
	}

}
