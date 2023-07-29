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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;

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
		.description("GrieferUtils meldet automatisch durch sich ausgelöste Fehler.")
		.config("settings.automatic_bug_reporting.enabled")
		.icon("bug")
		.defaultValue(true)
		.subSettings(uuid);

	private static final Set<String> reportedBugs = new HashSet<>();

	public static void reportError(Throwable error) {
		if (!enabled.get())
			return;

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
				HttpURLConnection conn = (HttpURLConnection) new URL("https://grieferutils.l3g7.dev/v2/bug_report").openConnection();

				if (conn instanceof HttpsURLConnection)
					((HttpsURLConnection) conn).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

				conn.addRequestProperty("User-Agent", "GrieferUtils v" + AddonUtil.getVersion());
				conn.setConnectTimeout(10000);
				conn.setDoOutput(true);
				conn.addRequestProperty("Content-Type", "text/plain");

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