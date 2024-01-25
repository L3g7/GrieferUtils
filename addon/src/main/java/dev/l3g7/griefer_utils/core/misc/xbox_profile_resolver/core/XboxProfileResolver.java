/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers.LabyModTokenProvider;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers.MinecraftTokenProvider;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers.MultiMCTokenProvider;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers.TokenProvider;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.DateTime;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.Requests;
import io.netty.util.internal.ConcurrentSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.Util.strMap;

public class XboxProfileResolver {

	private static boolean available = false;

	private static final Set<Long> requestSendTimes = new ConcurrentSet<>();

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
			.registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, type, ctx) -> new DateTime(json.getAsString()))
			.registerTypeAdapter(XboxProfile.class, (JsonDeserializer<XboxProfile>) (json, type, ctx) -> new XboxProfile(json.getAsJsonObject()))
			.setPrettyPrinting()
			.create();

	public static boolean isAvailable() {
		return available;
	}

	public static XboxProfile getProfileByXUID(String xuid) throws IOException {
		return getProfile("xuid", xuid);
	}

	public static XboxProfile getProfileByGamerTag(String gamerTag) throws IOException {
		return getProfile("gt", gamerTag);
	}

	private static XboxProfile getProfile(String key, String value) throws IOException {
		requestSendTimes.removeIf(time -> System.currentTimeMillis() - time > 15_000);
		if (requestSendTimes.size() >= 10)
			throw new IOException("Request would be ratelimited");

		requestSendTimes.add(System.currentTimeMillis());
		try {
			return GSON.fromJson(Requests.get("https://profile.xboxlive.com/users/" + key + "(" + value + ")/profile/settings?settings=GameDisplayName%2CGameDisplayPicRaw", strMap(
				"x-xbl-contract-version", "3",
				"Authorization", Authorization.getAuthorizationHeader(),
				"MS-CV", "wCV7YAWudea6DtP1.6",
				"Accept", "*/*",
				"Accept-Encoding", "identity",
				"User-Agent", "yes"
			)).getAsJsonObject().get("profileUsers").getAsJsonArray().get(0), XboxProfile.class);
		} catch (FileNotFoundException e) {
			// User not found
			return null;
		}
	}

	static {
		for (TokenProvider tokenProvider : new TokenProvider[]{new MinecraftTokenProvider(), new MultiMCTokenProvider(), new LabyModTokenProvider()}) {
			if (tokenProvider.load()) {
				available = true;
				break;
			}
		}

		if (!available)
			System.out.println("[XboxProfileResolver] No valid token could be found!");
	}

}
