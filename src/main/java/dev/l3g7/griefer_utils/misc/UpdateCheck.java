package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.features.misc.AutoUpdate;
import dev.l3g7.griefer_utils.features.misc.update_screen.UpdateScreen;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import net.labymod.addon.AddonLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.VersionUtil.getAddonVersion;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class UpdateCheck {

	private static final AutoUpdate autoUpdate = FileProvider.getSingleton(AutoUpdate.class);

	private static boolean isUpToDate = true;
	private static boolean triggeredShutdownHook = false;

	public static boolean isUpToDate() {
		return isUpToDate;
	}

	public static void checkForUpdate(UUID uuid) {
		if (!Config.has("version")) {
			// Starting for the first time -> Not updated
			Config.set("version", getAddonVersion());
			Config.save();
			return;
		}

		check(uuid);

		if (Config.get("version").getAsString().equals(getAddonVersion()))
			return;

		Config.set("version", getAddonVersion());
		Config.save();

		if (autoUpdate.showUpdateScreen.get())
			UpdateScreen.trigger();
	}

	private static void check(UUID addonUuid) {
		if (!autoUpdate.enabled.get())
			return;

		File currentAddonJar = AddonLoader.getFiles().get(addonUuid);
		if (currentAddonJar == null) {
			// Probably in dev environment, skip updating
			return;
		}

		try (InputStreamReader in = new InputStreamReader(new URL("https://api.github.com/repos/L3g7/GrieferUtils/releases").openConnection().getInputStream(), StandardCharsets.UTF_8)) {
			JsonObject latestRelease = null;
			for (JsonElement element : new JsonParser().parse(in).getAsJsonArray()) {
				JsonObject release = element.getAsJsonObject();
				if (release.get("prerelease").getAsBoolean())
					continue;

				latestRelease = release;
				break;
			}

			if (latestRelease == null)
				return;

			String tag = latestRelease.get("tag_name").getAsString().replaceFirst("v", "");
			if (tag.equals(getAddonVersion())) {
				if (!triggeredShutdownHook) {
					Runtime.getRuntime().addShutdownHook(new Thread(() -> check(addonUuid)));
					triggeredShutdownHook = true;
				}
				return;
			}

			// Get latest addon asset
			JsonArray assets = latestRelease.get("assets").getAsJsonArray();
			JsonObject asset = null;

			for (JsonElement jsonElement : assets) {
				JsonObject currentAsset = jsonElement.getAsJsonObject();
				if (currentAsset.get("name").getAsString().equals("griefer-utils-v" + tag + ".jar")) {
					asset = currentAsset;
					break;
				}
			}

			if (asset == null) {
				System.err.println("No correct GrieferUtils release could be found");
				return;
			}

			String downloadUrl = asset.get("browser_download_url").getAsString();

			HttpURLConnection conn;

			try {
				// Download new version
				conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
				conn.addRequestProperty("User-Agent", "GrieferUtils");

				File newAddonJar = new File(AddonLoader.getAddonsDirectory(), asset.get("name").getAsString());
				Files.copy(conn.getInputStream(), newAddonJar.toPath(), REPLACE_EXISTING);

				// Add old version to LabyMod's .delete
				Path deleteFilePath = AddonLoader.getDeleteQueueFile().toPath();
				String deleteLine = currentAddonJar.getName() + System.lineSeparator();
				Files.write(deleteFilePath, deleteLine.getBytes(), CREATE, APPEND);

				isUpToDate = false;

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException ignored) {}
	}

}
