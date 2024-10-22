/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.DebounceTimer;
import dev.l3g7.griefer_utils.core.api.misc.server.types.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.packets.transactions.Transaction;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_RECEIVE_PATTERN;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_SEND_PATTERN;
import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.split;
import static dev.l3g7.griefer_utils.core.api.util.CryptUtil.aesDigest;
import static dev.l3g7.griefer_utils.core.api.util.CryptUtil.rsaDigest;
import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.crypto.Cipher.DECRYPT_MODE;

public class LocalTransactions {

	private static final DebounceTimer TIMER = new DebounceTimer("Transactions-Save", 1000);
	private static final Gson GSON = new Gson();

	public static final Set<Transaction> transactions = Collections.synchronizedSet(new TreeSet<>());
	private static int idCounter = (int) (System.currentTimeMillis() & 0x3fffffff);

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(LocalTransactions::save));
	}

	@EventListener
	private static void onMsg(MessageReceiveEvent event) {
		Matcher matcher = PAYMENT_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		boolean received = matcher.matches();
		if (!received) {
			matcher = PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				return;
		}

		double amount = Double.parseDouble(matcher.group("amount").replace(",", ""));
		String nick = matcher.group("name").replaceAll("§.", "");
		String name = NameCache.ensureRealName(nick);
		UUID uuid = NameCache.getUUID(nick);
		if (uuid == null)
			uuid = mc().getNetHandler().getPlayerInfo(name).getGameProfile().getId();

		if (name.equals(name()) && received)
			// Show transactions to yourself only once
			return;

		Transaction transaction = new Transaction();
		transaction.id = idCounter++;
		transaction.username = received ? name : name();
		transaction.recipientname = received ? name() : name;
		transaction.userId = String.valueOf(received ? uuid : uuid());
		transaction.recipientId = String.valueOf(received ? uuid() : uuid);
		transaction.timestamp = System.currentTimeMillis();
		transaction.amount = amount;

		transactions.add(transaction);
		FileProvider.getBridge(TempTransactionsBridge.class).updateSettings();
		TIMER.schedule(LocalTransactions::save);
	}

	private static void save() {
		PlayerKeyPair.getPlayerKeyPair(mc().getSession().getToken()).thenAccept(pair -> {
			try {
				// Create file
				TransactionsFile file = new TransactionsFile();
				file.publicKey = pair.getPublicKey();
				file.expirationDate = pair.getExpirationTime();
				file.keySignature = pair.getPublicKeySignature();
				file.transactions = new ArrayList<>(transactions);

				// Sign file
				Signature signature = Signature.getInstance("SHA1withRSA");
				signature.initSign(pair.getPrivateKey());
				signature.update(GSON.toJson(file.transactions).getBytes(UTF_8));
				file.signature = Base64.getEncoder().encodeToString(signature.sign());

				// Save file
				IOUtil.write(new File("GrieferUtils/transactions/" + uuid() + ".transactions"), GSON.toJson(file).getBytes(UTF_8));
			} catch (Throwable e) {
				throw elevate(e);
			}
		});
	}

	@OnEnable
	private static void load() throws IOException {
		load(null);
	}

	@EventListener
	private static void load(AccountSwitchEvent ignored) throws IOException {
		transactions.clear();
		UUID uuid = uuid();
		File file = new File("GrieferUtils/transactions/" + uuid + ".transactions");
		if (!file.exists())
			return;

		byte[] bytes = IOUtil.toByteArray(IOUtil.read(file).getStream().orElseThrow(IOException::new));

		transactions.clear();
		decryptV1(bytes).thenAccept(success -> {
			if (!success)
				if (!decryptV2(uuid, bytes))
					LabyBridge.labyBridge.notifyMildError("Transaktionen fehlerhaft");
		});
	}

	private static CompletableFuture<Boolean> decryptV1(byte[] bytes) {
		CompletableFuture<Boolean> res = new CompletableFuture<>();
		PlayerKeyPair.getPlayerKeyPair(mc().getSession().getToken()).thenAccept(pair -> {
			try {
				Pair<byte[], byte[]> splitInput = split(bytes, 256);
				byte[] encKeyBytes = splitInput.getLeft();
				byte[] decKeyBytes = rsaDigest(pair.getPrivateKey(), encKeyBytes, DECRYPT_MODE);

				Pair<byte[], byte[]> splitKeyBytes = split(decKeyBytes, 16);
				SecretKey secretKey = new SecretKeySpec(splitKeyBytes.getLeft(), "AES");
				IvParameterSpec iv = new IvParameterSpec(splitKeyBytes.getRight());

				byte[] encData = splitInput.getRight();
				byte[] jsonBytes = aesDigest(secretKey, iv, encData, DECRYPT_MODE);
				String json = new String(jsonBytes, UTF_8);

				transactions.addAll(Arrays.asList(GSON.fromJson(json, Transaction[].class)));
			} catch (GeneralSecurityException | RuntimeException e) {
				res.complete(false);
				return;
			}
			res.complete(true);
		});
		return res;
	}

	private static boolean decryptV2(UUID uuid, byte[] bytes) {
		try {
			// Read transaction file
			TransactionsFile file = GSON.fromJson(new String(bytes, UTF_8), TransactionsFile.class);
			byte[] publicKeyBytes = Base64.getDecoder().decode(file.publicKey);
			byte[] keySignature = Base64.getDecoder().decode(file.keySignature);

			// Create payload for keySignature
			ByteBuffer keySignPayload = ByteBuffer.allocate(24 + publicKeyBytes.length);
			keySignPayload.putLong(uuid.getMostSignificantBits());
			keySignPayload.putLong(uuid.getLeastSignificantBits());
			keySignPayload.putLong(file.expirationDate);
			keySignPayload.put(publicKeyBytes);

			// Validate keySignature
			var data = IOUtil.read("https://api.minecraftservices.com/publickeys").asJsonObject().orElseThrow(IOException::new);
			boolean keySignatureValid = false;
			for (JsonElement yggKeyObj : data.get("playerCertificateKeys").getAsJsonArray()) {
				String yggKeyB64 = yggKeyObj.getAsJsonObject().get("publicKey").getAsString();
				byte[] yggKey = Base64.getDecoder().decode(yggKeyB64);
				X509EncodedKeySpec spec = new X509EncodedKeySpec(yggKey);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PublicKey yggPublicKey = keyFactory.generatePublic(spec);

				Signature signature = Signature.getInstance("SHA1withRSA");
				signature.initVerify(yggPublicKey);
				signature.update(keySignPayload.array());
				if (signature.verify(keySignature)) {
					keySignatureValid = true;
					break;
				}
			}

			if (!keySignatureValid)
				return false;

			// Validate signature
			byte[] signatureBytes = Base64.getDecoder().decode(file.signature);
			byte[] payload = GSON.toJson(file.transactions).getBytes(UTF_8);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(publicKey);
			signature.update(payload);
			if (!signature.verify(signatureBytes))
				return false;

			// Add transactions
			transactions.addAll(file.transactions);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static class TransactionsFile {

		public String publicKey;
		public long expirationDate;
		public String keySignature;
		public String signature;
		public List<Transaction> transactions;

	}

}
