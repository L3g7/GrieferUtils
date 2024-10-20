/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.auto_update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel;
import sun.misc.Unsafe;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.*;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel.STABLE;

/**
 * Checks whether GrieferUtils is up-to-date. If not, it downloads the newest release, replaces itself in
 * the classloader with the new jar, and deletes the old file on shutdown.
 * <p>
 * As loading any class would prevent it from being updated, this class contains code also found in
 * {@link dev.l3g7.griefer_utils.core.api.misc.config.Config},
 * {@link dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider},
 * {@link dev.l3g7.griefer_utils.core.api.util.IOUtil} and
 * {@link dev.l3g7.griefer_utils.core.api.reflection.Reflection}.
 */
@SuppressWarnings("CharsetObjectCanBeUsed") // Must be compatible with Java 8
public class AutoUpdater {

	// Let's Encrypt's ISRG ROOT X1 certificate
	// Used by the api server, api.grieferutils.l3g7.dev, and missing on older versions of Java, so it has to be added manually.
	private static final byte[] CERTIFICATE = Base64.getDecoder().decode("MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAwTzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2VhcmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJuZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBYMTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygch77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6UA5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sWT8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyHB5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UCB5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUvKBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWnOlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTnjh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbwqHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CIrU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkqhkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZLubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KKNFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7UrTkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdCjNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVcoyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPAmRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57demyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=");
	public static final String DELETION_MARKER = "File marked for deletion by GrieferUtils updater";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static MethodHandles.Lookup lookup;

	public static boolean hasUpdated = false;

	/**
	 * Lazily creates an unrestricted lookup and returns it.
	 */
	private static MethodHandles.Lookup getLookup() {
		if (lookup != null)
			return lookup;

		try {
			lookup = MethodHandles.lookup();
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			unsafe.putInt(lookup, 12, -1);
			return lookup;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean update(Init infoProvider) {
		try {
			String entrypoint = doUpdate(infoProvider);
			if (entrypoint == null)
				return false;

			Class<Entrypoint> c = (Class<Entrypoint>) Class.forName(entrypoint);
			c.getConstructor().newInstance().start();
			return true;
		} catch (IOException e) {
			// Allow start if updating failed due to network errors
			e.printStackTrace(System.err);
			return false;
		} catch (Throwable e) {
			throw new RuntimeException("Could not update GrieferUtils!", e);
		}
	}

	private static String doUpdate(Init infoProvider) throws Throwable {
		// Check if addon was loaded from a .jar file
		if (!AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile().contains(".jar"))
			return null;

		// Get own jar file
		File jarFile = new File(getOwnJar());

		// Delete old versions
		File[] addonJars = jarFile.getParentFile().listFiles((file, name) -> name.endsWith(".jar"));
		if (addonJars != null)
			for (File addonJar : addonJars)
				checkJarForDeletion(addonJar, infoProvider);

		if (!isEnabled())
			return null;

		JsonObject addonJson = getAddonJson();

		// Get info about the latest release
		String url = System.getProperty("griefer_utils.latest_release_url", "https://api.grieferutils.l3g7.dev/v6/latest_release/");
		InputStream in = read(url + addonJson.get("addonVersion").getAsString() + "/" + infoProvider.getLabyVersion() + "/");

		// Check if the server could be reached
		if (in == null)
			return null;

		Map<String, ReleaseInfo> releases = GSON.fromJson(new InputStreamReader(in), new TypeToken<Map<String, ReleaseInfo>>(){}.getType());
		in.close();

		// Get preferred release channel
		ReleaseChannel preferredChannel = getPreferredChannel();
		ReleaseInfo preferredRelease = releases.get(preferredChannel.name().toLowerCase());

		// If addon has debug mode enabled, compare versions
		if (addonJson.has("debug") && addonJson.get("debug").getAsBoolean()) {
			// Compare current version with latest version
			if (addonJson.get("addonVersion").getAsString().equals(preferredRelease.version))
				return null;
		} else {
			// Compare hash of own file with latest file hash
			if (hash(jarFile).equals(preferredRelease.hash))
				return null;
		}

		String version = preferredRelease.version;
		String downloadUrl = preferredChannel.downloadURL.replace("{version}", version);

		// Get target file
		File targetFile = new File(jarFile.getParentFile(), "griefer-utils-v" + version + ".jar");
		boolean shouldDownload = true;

		for (int suffix = 1; targetFile.exists(); suffix++) {
			if (!hash(targetFile).equals(preferredRelease.hash)) {
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

			// Check if the server could be reached
			if (in == null)
				return null;

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
		if (!jarFile.delete()) {
			// Minecraft's ClassLoader can create file leaks so the jar is probably locked.
			infoProvider.forceDeleteJar(jarFile);
		}

		// Hotswap new version
		if (!preferredRelease.hotswap)
			return null;

		removeURLFromClassLoaders(jarFile.toURI().toURL());

		MethodHandle addURL = getLookup().findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
		addURL.invoke(AutoUpdater.class.getClassLoader(), targetFile.toURI().toURL());

		hasUpdated = true;
		return preferredRelease.entrypoint.get(infoProvider.getLabyVersion());
	}

	private static void checkJarForDeletion(File file, Init infoProvider) throws IOException {
		ZipInputStream in = new ZipInputStream(Files.newInputStream(file.toPath()));
		ZipEntry entry = in.getNextEntry();

		if (entry == null || !DELETION_MARKER.equals(entry.getComment()))
			return;

		if (!file.delete())
			// Minecraft's ClassLoader can create file leaks so the jar is probably locked.
			infoProvider.forceDeleteJar(file);
	}

	private static ReleaseChannel getPreferredChannel() throws IOException {
		JsonObject config = getConfig();
		if (config == null || config.get("release_channel") == null)
			return STABLE;

		return ReleaseChannel.valueOf(config.get("release_channel").getAsString());
	}

	private static boolean isEnabled() throws IOException {
		JsonObject config = getConfig();
		if (config == null || !config.has("enabled"))
			return true;

		return config.get("enabled").getAsBoolean();
	}

	private static JsonObject getConfig() throws IOException {
		Path configPath = new File("config", "GrieferUtils.json").toPath();
		if (!Files.exists(configPath))
			return null;

		JsonObject config = Streams.parse(new JsonReader(new InputStreamReader(Files.newInputStream(configPath, StandardOpenOption.READ)))).getAsJsonObject();
		if (config.get("settings") == null)
			return null;

		config = config.get("settings").getAsJsonObject();
		if (config.get("auto_update") == null)
			return null;

		return config.get("auto_update").getAsJsonObject();
	}

	/**
	 * @return The path to the jar {@link AutoUpdater} was loaded from, in a format ready for URLs.
	 */
	private static String getOwnJar() {
		String ownJarUrl = AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile();

		if (ownJarUrl.contains("!"))
			ownJarUrl = ownJarUrl.substring(0, ownJarUrl.lastIndexOf("!")); // remove class

		try {
			ownJarUrl = URLDecoder.decode(ownJarUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		if (ownJarUrl.startsWith("file:/"))
			ownJarUrl = ownJarUrl.substring(5);

		return ownJarUrl;
	}

	/**
	 * Removes the given URL from the LaunchClassLoader and its parent.
	 */
	@SuppressWarnings("unchecked")
	private static void removeURLFromClassLoaders(URL urlToRemove) throws Throwable {

		// Create store accessors
		Class<?> urlClassPathClass;
		try {
			urlClassPathClass = Class.forName("jdk.internal.loader.URLClassPath");
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			urlClassPathClass = Class.forName("sun.misc.URLClassPath"); // NOTE: beautify
		}
		MethodHandle ucpGetter = getLookup().findGetter(URLClassLoader.class, "ucp", urlClassPathClass);
		MethodHandle pathGetter = getLookup().findGetter(urlClassPathClass, "path", ArrayList.class);
		MethodHandle lmapGetter = getLookup().findGetter(urlClassPathClass, "lmap", HashMap.class);
		MethodHandle loadersGetter = getLookup().findGetter(urlClassPathClass, "loaders", ArrayList.class);

		URLClassLoader classLoader = (URLClassLoader) AutoUpdater.class.getClassLoader();

		// Extract stores
		Object ucp = ucpGetter.invoke(classLoader);
		ArrayList<URL> path = (ArrayList<URL>) pathGetter.invoke(ucp);
		HashMap<String, Object> lmap = (HashMap<String, Object>) lmapGetter.invoke(ucp);
		ArrayList<Object> loaders = (ArrayList<Object>) loadersGetter.invoke(ucp);

		// Remove old URL
		path.remove(urlToRemove);
		Object loader = lmap.remove("file://" + urlToRemove.getFile());
		if (loader == null)
			return;

		loaders.remove(loader);
		((Closeable) loader).close();
	}

	/**
	 * Hashes the content of the given file using SHA-256 and returns the hex digest.
	 */
	private static final char[] HEX_CHARSET = "0123456789abcdef".toCharArray();

	private static String hash(File file) throws NoSuchAlgorithmException, IOException {
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
	 * @see dev.l3g7.griefer_utils.core.api.util.IOUtil
	 */
	private static InputStream read(String url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();

			if (conn instanceof HttpsURLConnection)
				((HttpsURLConnection) conn).setSSLSocketFactory(getCustomFactory());

			conn.addRequestProperty("User-Agent", "GrieferUtils");
			conn.setConnectTimeout(10000);
			return conn.getInputStream();
		} catch (IOException ignored) {
			return null;
		}
	}

	private static SSLSocketFactory customFactory = null;

	/**
	 * @see dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider
	 */
	private static SSLSocketFactory getCustomFactory() {
		if (customFactory != null)
			return customFactory;

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
			keyStore.setCertificateEntry(UUID.randomUUID().toString(), cf.generateCertificate(new ByteArrayInputStream(CERTIFICATE)));

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
	 *
	 * @see dev.l3g7.griefer_utils.core.api.file_provider.impl.JarFileProvider
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
				return Streams.parse(new JsonReader(new InputStreamReader(jarFile.getInputStream(addonJson.get())))).getAsJsonObject();
		}

		throw new FileNotFoundException("addon.json");
	}

	public interface Init {

		void forceDeleteJar(File jar) throws IOException;

		String getLabyVersion();

	}

	public interface Entrypoint {
		void start();
	}

}