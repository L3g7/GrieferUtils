package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.addon.AddonLoader;
import net.labymod.main.LabyMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.VersionUtil.getAddonVersion;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class UpdateCheck {

	private static boolean isUpToDate = true;

	public static boolean isUpToDate() {
		return isUpToDate;
	}

	private boolean shouldShowAchievement = false;

	public UpdateCheck() {
		if (!Config.has("version")) {
			// Starting for the first time -> Not updated
			Config.set("version", getAddonVersion());
			Config.save();
			return;
		}

		if (Config.get("version").getAsString().equals(getAddonVersion()))
			return;

		Config.set("version", getAddonVersion());
		Config.save();
		shouldShowAchievement = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTick(TickEvent.RenderTickEvent ignored) {
		// Using @LateInit results in the achievement display time being reduced due to heavy lag between the GuiOpenEvent and the first main menu render.
		// Using RenderTickEvent, the achievement gets displayed as soon as the main menu really renders.

		if (shouldShowAchievement) {
			LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("https://grieferutils.l3g7.dev/icon/64x64/", "Update wurde installiert.", "Der Changelog befindet sich in den Einstellungen.");
			shouldShowAchievement = false;
		}
	}

	public void checkForUpdate(UUID addonUuid) {
		File currentAddonJar = AddonLoader.getFiles().get(addonUuid);
		if (currentAddonJar == null) {
			// Probably in dev environment, skip updating
			return;
		}

		IOUtil.request("https://api.github.com/repos/L3g7/GrieferUtils/releases").asJsonArray(releases -> {
			JsonObject latestRelease = releases.get(0).getAsJsonObject();

			String tag = latestRelease.get("tag_name").getAsString().replaceFirst("v", "");
			if (tag.equals(getAddonVersion()))
				return;

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
		});
	}

}
