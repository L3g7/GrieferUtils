/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.api.misc.server.types.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.ByteAndBit.BAB_URL;

public class BABBot {

	public List<BABItem> items;
	public AxisAlignedBB botZone;
	public final String uuid;
	private long lastSync = 0;
	final static int guiSyncDebounce = 20000;

	public BABBot(String name) {
		this.uuid = name;
	}

	public String getName() {
		return NameCache.getName(UUID.fromString(this.uuid.replaceAll("^(.{8})(.{4})(.{4})(.{4})(.{12})$", "$1-$2-$3-$4-$5")));
	}

	public void invalidateCache() {
		this.botZone = null;
	}

	public boolean isVecInsideOrTouching(Vec3 vec) {
		AxisAlignedBB zone = botZone; // Copy botZone to prevent errors because of concurrent modifications
		if (zone == null)
			return false;

		if (vec.xCoord < zone.minX || vec.xCoord > zone.maxX)
			return false;

		if (vec.yCoord < zone.minY || vec.yCoord > zone.maxY)
			return false;

		return !(vec.zCoord < zone.minZ) && !(vec.zCoord > zone.maxZ);
	}

	private JsonObject getItems() throws IOException, GeneralSecurityException, ExecutionException, InterruptedException {
		PlayerKeyPair kp = PlayerKeyPair.getPlayerKeyPair(mc().getSession().getToken()).get();
		UUID user = MinecraftUtil.uuid();
		long timestamp = new Date().getTime();
		// Create signature
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(kp.getPrivateKey());
		sign.update(String.valueOf(timestamp).getBytes());

		String signature = Base64.getEncoder().encodeToString(sign.sign());
		String publicKey = kp.getPublicKey();
		String keySignature = kp.getPublicKeySignature();
		long expirationTime = kp.getExpirationTime();

		InputStream is = postRequest(BAB_URL + "item/getItems/" + uuid, new NameValuePair[]{
			new BasicNameValuePair("publickey", publicKey),
			new BasicNameValuePair("signature", signature),
			new BasicNameValuePair("keySignature", keySignature),
			new BasicNameValuePair("timestamp", Long.toString(timestamp)),
			new BasicNameValuePair("expirationTime", Long.toString(expirationTime)),
			new BasicNameValuePair("uuid", user.toString().replaceAll("-", "")),
		});
		return IOUtil.jsonParser.parse(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	public CompletableFuture<Boolean> sync() {
		if (botZone != null && System.currentTimeMillis() - lastSync < guiSyncDebounce)
			return CompletableFuture.supplyAsync(() -> true);

		lastSync = System.currentTimeMillis();
		return CompletableFuture.supplyAsync(() -> {
			try {
				JsonObject res = getItems();
				if (res == null) return false;
				if (!res.get("success").getAsBoolean()) return false;
				JsonArray array = res.get("items").getAsJsonArray();
				JsonObject aabb = res.get("zone").getAsJsonObject();
				this.items = BABItem.parse(array);
				this.botZone = AxisAlignedBB.fromBounds(aabb.get("x1").getAsInt(), aabb.get("y1").getAsInt(), aabb.get("z1").getAsInt(), aabb.get("x2").getAsInt(), aabb.get("y2").getAsInt(), aabb.get("z2").getAsInt());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		});
	}

	private InputStream postRequest(String url, NameValuePair[] body) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		if (conn instanceof HttpsURLConnection)
			((HttpsURLConnection) conn).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		conn.addRequestProperty("User-Agent", "GrieferUtils v" + LabyBridge.labyBridge.addonVersion());
		conn.setConnectTimeout(10000);
		conn.setDoOutput(true);
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestMethod("POST");

		try (OutputStream out = conn.getOutputStream()) {
			new UrlEncodedFormEntity(Arrays.asList(body), "UTF-8").writeTo(out);
			out.flush();
		}

		return conn.getInputStream();
	}


}