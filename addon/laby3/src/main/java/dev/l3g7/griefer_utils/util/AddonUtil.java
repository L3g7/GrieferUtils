/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.utils.JsonParse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * A utility class for addon information.
 */
public class AddonUtil {

	private static String addonVersion = null;
	private static AddonInfo addonInfo = null;

	public static UUID getUUID() {
		return Main.getInstance().about.uuid;
	}

	/**
	 * Gets the addon version without requiring the addon to be loaded by LabyMod.
	 * @return The GrieferUtils version.
	 */
	public static String getVersion() {
		if (addonVersion != null)
			return addonVersion;

		try {
			String addonJson = IOUtils.toString(FileProvider.getData("addon.json"));
			addonVersion = JsonParse.parse(addonJson).getAsJsonObject().get("addonVersion").getAsString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return addonVersion;
	}

	/**
	 * @return The AddonInfo object for GrieferUtils.
	 */
	public static AddonInfo getInfo() {
		if (addonInfo != null)
			return addonInfo;

		AddonInfoManager addonInfoManager = AddonInfoManager.getInstance();
		addonInfoManager.init();

		UUID addonUuid = getUUID();

		// Retrieve addonInfo
		addonInfo = addonInfoManager.getAddonInfoMap().get(addonUuid);
		if (addonInfo == null)
			addonInfo = AddonLoader.getOfflineAddons().stream().filter(addon -> addon.getUuid().equals(addonUuid)).findFirst().orElse(null);

		if (addonInfo == null)
			throw new NullPointerException("Addon-Infos konnten nicht geladen werden!");

		return addonInfo;
	}

}
