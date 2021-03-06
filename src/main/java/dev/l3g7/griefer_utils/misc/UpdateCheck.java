package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.labymod.addon.AddonLoader;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class UpdateCheck {

	private static boolean isUpToDate = true;

	public static void checkForUpdate(UUID addonUuid) {
		File currentAddonJar = AddonLoader.getFiles().get(addonUuid);
		if(currentAddonJar == null) {
			// Probably in dev environment, skip updating
			return;
		}

		IOUtil.request("https://api.github.com/repos/L3g7/GrieferUtils/releases").asJsonArray(releases -> {
			JsonObject latestRelease = releases.get(0).getAsJsonObject();

			String tag = latestRelease.get("tag_name").getAsString().replaceFirst("v", "");
			if(tag.equals(VersionUtil.getAddonVersion())) {
				return;
			}

			JsonObject asset = latestRelease.get("assets").getAsJsonArray().get(0).getAsJsonObject();
			String downloadUrl = asset.get("browser_download_url").getAsString();

			HttpURLConnection conn;

			try {
				conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
				conn.addRequestProperty("User-Agent", "GrieferUtils");

				File newAddonJar = new File((File) Reflection.get(AddonLoader.class, "addonsDirectory"), asset.get("name").getAsString());

				Files.copy(conn.getInputStream(), newAddonJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

				Runtime.getRuntime().addShutdownHook(new Thread(currentAddonJar::delete));

				isUpToDate = false;

			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean isUpToDate() {
		return isUpToDate;
	}

}
