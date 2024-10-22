/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api;

import dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

public class BugReporter {

	public static final SwitchSetting shouldSendUuid = SwitchSetting.create()
		.name("UUID mitsenden")
		.description("Ob deine Minecraft-UUID als Kontaktmöglichkeit mitgesendet werden soll.")
		.config("settings.automatic_bug_reporting.uuid")
		.icon("steve")
		.defaultValue(true);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatische Fehlermeldung")
		.description("Meldet automatisch durch GrieferUtils ausgelöste Fehler.")
		.config("settings.automatic_bug_reporting.enabled")
		.icon("bug")
		.defaultValue(true)
		.subSettings(shouldSendUuid);

	private static final Set<String> reportedBugs = new HashSet<>();
	private static long timestampOfLastReport = 0;

	private static boolean shouldReportError(Throwable error) {
		if (!labyBridge.obfuscated())
			return false;

		if (System.currentTimeMillis() - timestampOfLastReport < 10_000)
			return false; // Last report less than 10s ago, don't report

		// Don't report OutOfMemoryErrors
		if (error instanceof OutOfMemoryError) {
			try {
				System.gc();
			} catch (Throwable ignored) {
			}
			return false;
		}

		// Check cause
		return error.getCause() == null || !shouldReportError(error.getCause());
	}

	public static void reportError(Throwable error) {
		error.printStackTrace(System.err);
		if (!enabled.get() || !shouldReportError(error))
			return;

		labyBridge.notifyError("Ein unbekannter Fehler ist aufgetreten!");

		timestampOfLastReport = System.currentTimeMillis();
		Thread t = new Thread(() -> {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				// Get stacktrace
				error.printStackTrace(new PrintStream(out));
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				String stackTrace = out.toString("UTF-8");

				// Remove object hash codes
				stackTrace = stackTrace
					.replaceAll("\tat sun.*\r?\n", "")
					.replaceAll("@[\\da-f]+", "")
					.replaceAll("\\$\\$Lambda[\\d/$]*\\.(\\w+)(?=\\()", "\\$\\$Lambda.$1");

				// Don't report if already reported
				String hash = Base64.getEncoder().encodeToString(digest.digest(stackTrace.getBytes()));
				if (!reportedBugs.add(hash))
					return;
			} catch (IOException | NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			// Report bug
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL("https://grieferutils.l3g7.dev/v6/bug_report").openConnection();

				if (conn instanceof HttpsURLConnection)
					((HttpsURLConnection) conn).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

				conn.setConnectTimeout(10000);
				conn.setDoOutput(true);

				conn.addRequestProperty("User-Agent", "GrieferUtils v" + labyBridge.addonVersion());
				conn.addRequestProperty("Content-Type", "text/plain");
				conn.addRequestProperty("X-MINECRAFT-FORGE", String.valueOf(labyBridge.forge()));
				conn.addRequestProperty("X-LABYMOD-4", String.valueOf(LABY_4.isActive()));

				if (shouldSendUuid.get())
					conn.addRequestProperty("X-MINECRAFT-UUID", String.valueOf(MinecraftUtil.uuid()));

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