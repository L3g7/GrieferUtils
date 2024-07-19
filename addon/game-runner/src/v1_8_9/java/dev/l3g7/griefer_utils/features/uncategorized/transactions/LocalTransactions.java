/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.DebounceTimer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.misc.server.types.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.api.util.CryptUtil;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.packets.transactions.Transaction;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.PublicKey;
import java.util.*;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_RECEIVE_PATTERN;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.PAYMENT_SEND_PATTERN;
import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.merge;
import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.split;
import static dev.l3g7.griefer_utils.core.api.util.CryptUtil.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

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
		String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
		UUID uuid = mc().getNetHandler().getPlayerInfo(name).getGameProfile().getId();

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
			String json = GSON.toJson(transactions);
			byte[] jsonBytes = json.getBytes(UTF_8);

			IvParameterSpec iv = generateIv();
			SecretKey secretKey = generateKey();
			PublicKey publicKey = CryptUtil.decodePublicKey(Base64.getDecoder().decode(pair.getPublicKey()));

			byte[] decKeyBytes = merge(secretKey.getEncoded(), iv.getIV());
			byte[] encKeyBytes = rsaDigest(publicKey, decKeyBytes, ENCRYPT_MODE);

			byte[] encData = aesDigest(secretKey, iv, jsonBytes, ENCRYPT_MODE);
			byte[] result = merge(encKeyBytes, encData);

			IOUtil.write(new File("GrieferUtils/transactions/" + uuid() + ".transactions"), result);
		});
	}

	@EventListener
	private static void load(GrieferGamesJoinEvent ignored) {
		transactions.clear();
		PlayerKeyPair.getPlayerKeyPair(mc().getSession().getToken()).thenAccept(pair -> {
			File file = new File("GrieferUtils/transactions/" + uuid() + ".transactions");
			if (!file.exists())
				return;

			IOUtil.read(file).getStream().ifPresent(in -> {
				byte[] bytes = elevateErrors(() -> IOUtil.toByteArray(in));

				Pair<byte[], byte[]> splitInput = split(bytes, 256);
				byte[] encKeyBytes = splitInput.getLeft();
				byte[] decKeyBytes = rsaDigest(elevateErrors(pair::getPrivateKey), encKeyBytes, DECRYPT_MODE);

				Pair<byte[], byte[]> splitKeyBytes = split(decKeyBytes, 16);
				SecretKey secretKey = new SecretKeySpec(splitKeyBytes.getLeft(), "AES");
				IvParameterSpec iv = new IvParameterSpec(splitKeyBytes.getRight());

				byte[] encData = splitInput.getRight();
				byte[] jsonBytes = aesDigest(secretKey, iv, encData, DECRYPT_MODE);
				String json = new String(jsonBytes, UTF_8);

				for (JsonElement transactionJson : JsonParser.parseString(json).getAsJsonArray())
					transactions.add(GSON.fromJson(transactionJson, Transaction.class));
			});
		});
	}

	private static <T> T elevateErrors(Supplier<T> supplier) {
		return supplier.get();
	}

}
