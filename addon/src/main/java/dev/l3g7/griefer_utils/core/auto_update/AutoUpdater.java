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

package dev.l3g7.griefer_utils.core.auto_update;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel;
import net.labymod.addon.AddonLoader;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel.STABLE;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Checks whether GrieferUtils is up-to-date. If not, it downloads the newest release, replaces itself in
 * the classloader with the new jar, and deletes the old file on shutdown.
 * <p>
 * As loading any class would prevent it from being updated, this class contains code also found in
 * {@link dev.l3g7.griefer_utils.core.misc.config.Config},
 * {@link dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider},
 * {@link dev.l3g7.griefer_utils.core.util.IOUtil} and
 * {@link dev.l3g7.griefer_utils.core.reflection.Reflection}.
 */
public class AutoUpdater {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final JsonParser PARSER = new JsonParser();
	public static boolean hasUpdated = false;

	public static void update() throws IOException, NoSuchAlgorithmException, InterruptedException, ReflectiveOperationException {
		if (!isEnabled())
			return;

		// Check if Updater was loaded from a .jar file
		if (!AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile().contains(".jar"))
			return;

		// Get info about the latest release
		InputStream in = read("https://grieferutils.l3g7.dev/v2/latest_release");
		@SuppressWarnings("UnstableApiUsage")
		Map<String, ReleaseInfo> releases = GSON.fromJson(new InputStreamReader(in), new TypeToken<Map<String, ReleaseInfo>>(){}.getType());
		in.close();

		// Get preferred release channel
		ReleaseChannel preferredChannel = getPreferredChannel();
		ReleaseInfo preferredRelease = releases.get(preferredChannel.name().toLowerCase());

		// Get own jar file
		String ownJarUrl = getOwnJar();
		File jarFile = new File(ownJarUrl.substring(5)); // Remove protocol from jar path

		// If addon has debug mode enabled, compare versions
		JsonObject addonJson = getAddonJson();
		if (addonJson.has("debug") && addonJson.get("debug").getAsBoolean()) {
			// Compare current version with latest version
			if (addonJson.get("addonVersion").getAsString().equals(preferredRelease.version))
				return;
		} else {
			// Compare hash of own file with latest file hash
			if (hash(jarFile).equals(preferredRelease.hash))
				return;
		}

		String version = preferredRelease.version;
		String downloadUrl = preferredChannel.downloadURL.replace("{version}", version);

		// Get target file
		File targetFile = new File(jarFile.getParentFile(), "griefer-utils-v" + version + ".jar");
		boolean shouldDownload = true;

		for (int suffix = 1; targetFile.exists(); suffix++) {
			if (!hash(targetFile).equals(preferredRelease.hash)) {
				// Hash doesn't match, file corrupt? Try deleting
				if (deleteJarSilently(targetFile.getAbsolutePath()))
					break;

				// File could not be deleted for now, use other file
				targetFile = new File(jarFile.getParentFile(), "griefer-utils-v" + version + " - auto-updated #" + suffix + ".jar");
			} else {
				// Hash matches, don't download again
				shouldDownload = false;
				break;
			}
		}

		// Download target file
		if (shouldDownload) {
			in = read(downloadUrl);
			Files.copy(in, targetFile.toPath());
			in.close();
		}

		// Check if hash matches for new file
		if (!hash(targetFile).equals(preferredRelease.hash)) {
			// Hash doesn't match, file corrupt? Revert update
			Files.delete(targetFile.toPath());
			throw new IllegalStateException("New version downloaded but is corrupted");
		}

		// New version downloaded successfully, remove old version
		removeURLFromClassLoaders(new URL(ownJarUrl));
		deleteJarSilently(ownJarUrl.substring(6));

		// Load new version
		Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
		addURL.invoke(Launch.classLoader, targetFile.toURI().toURL());

		hasUpdated = true;
	}

	/**
	 * @return Whether the file was deleted instantly.
	 */
	private static boolean deleteJarSilently(String path) throws IOException {
		// Try to delete file directly
		if (new File(path).delete())
			return true;

		// Probably locked; Overwrite it with an empty zip file until Minecraft is closed
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bout);
		out.setComment("File marked for deletion by GrieferUtils updater");
		out.close();
		Files.write(Paths.get(path), bout.toByteArray());

		// Add old file to LabyMod's .delete
		Path deleteFilePath = AddonLoader.getDeleteQueueFile().toPath();
		String deleteLine = new File(path).getName() + System.lineSeparator();
		Files.write(deleteFilePath, deleteLine.getBytes(), CREATE, APPEND);
		return false;
	}

	private static ReleaseChannel getPreferredChannel() throws IOException {
		Path configPath = new File("config", "GrieferUtils.json").toPath();
		if (!Files.exists(configPath))
			return STABLE;

		JsonObject config = PARSER.parse(new InputStreamReader(Files.newInputStream(configPath, StandardOpenOption.READ))).getAsJsonObject();
		if (!config.has("settings"))
			return STABLE;

		config = config.get("settings").getAsJsonObject();
		if (!config.has("auto_update"))
			return STABLE;

		config = config.get("auto_update").getAsJsonObject();
		if (!config.has("release_channel"))
			return STABLE;

		return ReleaseChannel.valueOf(config.get("release_channel").getAsString());
	}

	private static boolean isEnabled() throws IOException {
		Path configPath = new File("config", "GrieferUtils.json").toPath();
		if (!Files.exists(configPath))
			return true;

		JsonObject config = PARSER.parse(new InputStreamReader(Files.newInputStream(configPath, StandardOpenOption.READ))).getAsJsonObject();
		if (!config.has("settings"))
			return true;

		config = config.get("settings").getAsJsonObject();
		if (!config.has("auto_update"))
			return true;

		config = config.get("auto_update").getAsJsonObject();
		if (!config.has("enabled"))
			return true;

		return config.get("enabled").getAsBoolean();
	}

	/**
	 * @return The path to the jar {@link AutoUpdater} was loaded from, in a format ready for URLs.
	 */
	private static String getOwnJar() throws UnsupportedEncodingException {
		String ownJarUrl = AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		ownJarUrl = ownJarUrl.substring(0, ownJarUrl.lastIndexOf("!")); // remove class
		return URLDecoder.decode(ownJarUrl, "UTF-8");
	}

	/**
	 * Removes the given URL from the LaunchClassLoader and its parent.
	 */
	@SuppressWarnings("unchecked")
	private static void removeURLFromClassLoaders(URL urlToRemove) throws ReflectiveOperationException, IOException {
		// Create fields for access to stores
		Class<?> urlClassPathClass = Class.forName("sun.misc.URLClassPath");
		Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
		ucpField.setAccessible(true);
		Field pathField = urlClassPathClass.getDeclaredField("path");
		pathField.setAccessible(true);
		Field lmapField = urlClassPathClass.getDeclaredField("lmap");
		lmapField.setAccessible(true);
		Field loadersField = urlClassPathClass.getDeclaredField("loaders");
		loadersField.setAccessible(true);

		Field parentField = LaunchClassLoader.class.getDeclaredField("parent");
		parentField.setAccessible(true);
		for (URLClassLoader classLoader : new URLClassLoader[] {Launch.classLoader, (URLClassLoader) parentField.get(Launch.classLoader)}) {
			// Extract stores
			Object ucp = ucpField.get(classLoader);
			ArrayList<URL> path = (ArrayList<URL>) pathField.get(ucp);
			HashMap<String, Object> lmap = (HashMap<String, Object>) lmapField.get(ucp);
			ArrayList<Object> loaders = (ArrayList<Object>) loadersField.get(ucp);

			// Remove old URL
			path.remove(urlToRemove);
			Object loader = lmap.remove("file://" + urlToRemove.getFile());
			if (loader == null)
				continue;
			loaders.remove(loader);
			((Closeable) loader).close();
		}

		// Disable all lookup caches
		Method disableAllLookupCaches = urlClassPathClass.getDeclaredMethod("disableAllLookupCaches");
		disableAllLookupCaches.setAccessible(true);
		disableAllLookupCaches.invoke(null);
	}

	/**
	 * Hashes the content of the given file using SHA-256 and returns the hex digest.
	 */
	private static final char[] HEX_CHARSET = "0123456789abcdef".toCharArray();
	public static String hash(File file) throws NoSuchAlgorithmException, IOException {
		byte[] bytes = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
		char[] res = new char[bytes.length * 2];
		for (int i = 0; i < res.length; i += 2) {
			int b = bytes[i / 2] & 0xFF;
			res[i] = HEX_CHARSET[b >>> 4];
			res[i + 1] = HEX_CHARSET[b & 0x0F];
		}
		return new String(res);
	}

	/**
	 * @see dev.l3g7.griefer_utils.core.util.IOUtil
	 */
	public static InputStream read(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		if (conn instanceof HttpsURLConnection)
			((HttpsURLConnection) conn).setSSLSocketFactory(getCustomFactory());

		conn.addRequestProperty("User-Agent", "GrieferUtils");
		conn.setConnectTimeout(10000);
		return conn.getInputStream();
	}

	private static SSLSocketFactory customFactory = null;

	/**
	 * @see dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider
	 */
	private static SSLSocketFactory getCustomFactory() {
		if (customFactory != null)
			return customFactory;

		// DigiCert's Global Root G2 certificate
		// Used by the api server, l3g7.dev, and missing on older versions of Java, so is has to be added manually.
		byte[] digiCertCertificate = Base64.getDecoder().decode("MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBhMQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBHMjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVTMRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5jb20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQq2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5WztCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQvIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NGFdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ918rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTepLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTflMrY=");
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);

			// Load default certs
			String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
			KeyStore defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			defaultKeyStore.load(Files.newInputStream(Paths.get(filename)), "changeit".toCharArray());

			for (TrustAnchor ta : new PKIXParameters(defaultKeyStore).getTrustAnchors())
				keyStore.setCertificateEntry(UUID.randomUUID().toString(), ta.getTrustedCert());

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			// Add DigiCert's certificate
			keyStore.setCertificateEntry(UUID.randomUUID().toString(), cf.generateCertificate(new ByteArrayInputStream(digiCertCertificate)));

			// Create factory
			SSLContext sslContext = SSLContext.getInstance("SSL");
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
			customFactory = sslContext.getSocketFactory();
			return customFactory;
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Searches the addon's addon.json file and returns the json content.
	 * @see dev.l3g7.griefer_utils.core.file_provider.impl.JarFileProvider
	 */
	private static JsonObject getAddonJson() throws IOException {
		String jarPath = AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (!jarPath.contains(".jar"))
			throw new IllegalStateException("Invalid code source location: " + jarPath);

		// Sanitize jarPath
		jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
		jarPath = URLDecoder.decode(jarPath, "UTF-8");

		// Read entries
		try (JarFile jarFile = new JarFile(jarPath)) {
			if (jarFile.size() == 0)
				throw new IllegalStateException("Empty jar file: " + jarPath);

			Optional<JarEntry> addonJson = jarFile.stream().filter(entry -> entry.getName().equals("addon.json")).findFirst();
			if (addonJson.isPresent())
				return PARSER.parse(new InputStreamReader(jarFile.getInputStream(addonJson.get()))).getAsJsonObject();
		}

		throw new FileNotFoundException("addon.json");
	}

}
