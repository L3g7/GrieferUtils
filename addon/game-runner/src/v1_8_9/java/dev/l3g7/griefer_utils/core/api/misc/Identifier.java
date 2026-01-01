/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SuppressWarnings("ReadWriteStringCanBeUsed")
public class Identifier {

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static final Identifier MACHINE_IDENT = new Identifier(Paths.get(System.getProperty("user.home"), ".griefer_utils.id"));
	public static final Identifier CWD_IDENT = new Identifier(Paths.get(".griefer_utils.id"));

	private final String identifier;

	@OnEnable
	private static void init() {
		// Load identifiers
	}

	private Identifier(Path path) {
		try {
			if (Files.exists(path)) {
				identifier = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			} else {
				// Generate ident
				byte[] data = new byte[16];
				SecureRandom.getInstanceStrong().nextBytes(data);
				identifier = bytesToHex(data);

				Files.write(path, identifier.getBytes(StandardCharsets.UTF_8));

				try {
					Files.setAttribute(path, "dos:hidden", true);
					Files.setAttribute(path, "dos:system", true);
					Files.setAttribute(path, "dos:readonly", true);
				} catch (Throwable ignored) {
					// Don't care
				}
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			throw Util.elevate(e);
		}
	}

	@Override
	public String toString() {
		return identifier;
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

}
