package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.addon.AddonLoader;
import net.labymod.main.LabyMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.VersionUtil.getAddonVersion;

public class UpdateCheck {

	private static boolean isUpToDate = true;

	public static boolean isUpToDate() {
		return isUpToDate;
	}

	private boolean shouldShowAchievement = false;

	public UpdateCheck() {
		// Starting for the first time -> Not updated
		if (!Config.has("version")) {
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
		if (shouldShowAchievement) { // I have to use RenderTickEvent instead of GuiOpenEvent since the main gui is opened before it is rendered -> Achievement is only shown for ~1s
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
			if (tag.equals(getAddonVersion())) {
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
}
