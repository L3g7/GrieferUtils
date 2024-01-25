/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.network.ThreadQuickExitException;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class BugReporter {

	private static final BooleanSetting uuid = new BooleanSetting()
		.name("UUID mitsenden")
		.description("Ob deine Minecraft-UUID als Kontaktmöglichkeit mitgesendet werden soll.")
		.config("settings.automatic_bug_reporting.uuid")
		.icon("steve")
		.defaultValue(true);

	public static final BooleanSetting enabled = new BooleanSetting()
		.name("Automatische Fehlermeldung")
		.description("Meldet automatisch durch GrieferUtils ausgelöste Fehler.")
		.config("settings.automatic_bug_reporting.enabled")
		.icon("bug")
		.defaultValue(true)
		.subSettings(uuid);

	private static final Set<String> reportedBugs = new HashSet<>();
	private static long timestampOfLastReport = 0;

	private static boolean shouldReportError(Throwable error) {
		if (!LabyModCoreMod.isObfuscated())
			return false;

		if (System.currentTimeMillis() - timestampOfLastReport < 10_000)
			return false; // Last report less than 10s ago, don't report

		if (error instanceof ThreadQuickExitException)
			return false;

		// Don't report OutOfMemoryErrors
		if (error instanceof OutOfMemoryError) {
			try {
				System.gc();
			} catch (Throwable ignored) {}
			return false;
		}

		// Check cause
		return error.getCause() == null || !shouldReportError(error.getCause());
	}

	public static void reportError(Throwable error) {
		error.printStackTrace();
		if (!enabled.get() || !shouldReportError(error))
			return;

		MinecraftUtil.displayAchievement("§cGrieferUtils", "§cEs gab einen Fehler :(");

		timestampOfLastReport = System.currentTimeMillis();
		Thread t = new Thread(() -> {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				// Get stacktrace
				error.printStackTrace(new PrintStream(out));
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				String stackTrace = new String(out.toByteArray(), StandardCharsets.UTF_8);

				// Remove object hash codes
				stackTrace = stackTrace.replaceAll("\\$\\$Lambda[\\d/$]*\\.(\\w+)(?=\\()", "\\$\\$Lambda").replaceAll("@[\\da-f]+", "");

				// Don't report if already reported
				String hash = Base64.getEncoder().encodeToString(digest.digest(stackTrace.getBytes()));
				if (!reportedBugs.add(hash))
					return;
			} catch (IOException | NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			// Report bug
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL("https://grieferutils.l3g7.dev/v3/bug_report").openConnection();

				if (conn instanceof HttpsURLConnection)
					((HttpsURLConnection) conn).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

				conn.addRequestProperty("User-Agent", "GrieferUtils v" + AddonUtil.getVersion());
				conn.setConnectTimeout(10000);
				conn.setDoOutput(true);
				conn.addRequestProperty("Content-Type", "text/plain");
				conn.addRequestProperty("X-MINECRAFT-FORGE", String.valueOf(LabyModCoreMod.isForge()));

				if (uuid.get())
					conn.addRequestProperty("X-MINECRAFT-UUID", MinecraftUtil.uuid().toString());

				conn.setRequestMethod("POST");
				try (OutputStream out = conn.getOutputStream()) {
					error.printStackTrace(new PrintStream(out));
					out.flush();
				}
				conn.getInputStream().close();
				conn.disconnect();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}, "GrieferUtils automatic bug reporter");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

}