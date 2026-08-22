/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list.player;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Predicate;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Runnable;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.util.io.HttpGetOperation;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.lang.Thread.MAX_PRIORITY;

public class Resolver {

	protected static final RuntimeException OK_FOUND = new RuntimeException();
	protected static final RuntimeException ERR_NOT_FOUND = new RuntimeException();
	protected static final RuntimeException ERR_INVALID = new RuntimeException();

	public static void resolve(PlayerListEntry entry) {
		// Try to load from tablist
		Predicate<GameProfile> check = entry.id == null
			? n -> n.getName().equals(entry.name)
			: n -> n.getId().toString().equals(entry.id);

		if (mc().getNetHandler() != null) {
			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
				if (check.test(info.getGameProfile())) {
					entry.id = info.getGameProfile().getId().toString();
					entry.name = info.getGameProfile().getName();
					TickScheduler.runNextRenderTick(() -> {
						entry.skin = new SimpleTexture(info.getLocationSkin());
						entry.skin.loadTexture(mc().getResourceManager());
					});
					updateCache(entry, true);
					return;
				}
			}
		}

		ThreadFactory.run("Grieferutils PlayerListEntry Resolver", MAX_PRIORITY, () -> {
			Result res = entry.isMojang()
				? JavaPlayerListEntryResolver.load(entry)
				: BedrockPlayerListEntryResolver.load(entry);

			if (res == Result.ERROR)
				return;

			updateCache(entry, res == Result.FOUND);
		});
	}

	private static void updateCache(PlayerListEntry entry, boolean found) {
		entry.exists = entry.loaded = found;
		PlayerListEntry.NAME_LOOKUP_MAP.put(entry.name, entry);
		PlayerListEntry.UUID_LOOKUP_MAP.put(entry.id, entry);
	}

	protected static void checkHttpCode(HttpGetOperation op) {
		checkHttpCode(op, 404);
	}

	protected static void checkHttpCode(HttpGetOperation op, int notFound) {
		if (op.getResponseCode() == notFound || op.getResponseCode() == 204)
			throw ERR_NOT_FOUND;

		if (op.getResponseCode() != 200)
			throw ERR_INVALID;
	}

	protected static void loadSkin(PlayerListEntry entry, byte[] texture) throws IOException {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(texture));
		entry.slim = img.getHeight() == 32;

		TickScheduler.runNextRenderTick(() -> {
			entry.skin = new DynamicTexture(img);
			entry.skin.loadTexture(mc().getResourceManager());
		});
	}

	protected static String getNameOrUUID(PlayerListEntry entry) {
		if (entry.id != null)
			return entry.id;

		if (entry.isMojang())
			return entry.name;
		else
			return entry.name.substring(1);
	}

	protected enum Result {
		ERROR,
		NOT_FOUND,
		FOUND;

		public static Result get(Supplier<Result> supplier) {
			try {
				return supplier.getWithThrowable();
			} catch (Throwable t) {
				if (t == ERR_NOT_FOUND)
					return NOT_FOUND;

				return ERROR;
			}
		}

		public static void tryLoad(Runnable runnable) {
			Result res = get(() -> {
				runnable.runWithThrowable();
				return FOUND;
			});

			switch (res) {
				case FOUND -> throw OK_FOUND;
				case NOT_FOUND -> throw ERR_NOT_FOUND;
				case ERROR -> {
					// No-op
				}
			}
		}
	}

}
