/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import com.google.gson.JsonObject;
import com.sun.jna.Platform;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.mapping.Mapping;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.main.LabyMod;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.*;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class LabyBridgeImpl implements LabyBridge {

	private JsonObject addonJson = null;

	@Override
	public boolean obfuscated() {
		return LabyModCoreMod.isObfuscated();
	}

	@Override
	public boolean inDevEnv() {
		return !obfuscated();
	}

	@Override
	public Mapping activeMapping() {
		return obfuscated() ? forge() ? SEARGE : OBFUSCATED : UNOBFUSCATED;
	}

	@Override
	public boolean forge() {
		return LabyModCoreMod.isForge();
	}

	private JsonObject getAddonJson() {
		if (addonJson != null)
			return addonJson;

		addonJson = IO.read(FileProvider.getData("addon.json")).asJsonObject();
		return addonJson;
	}

	@Override
	public String addonVersion() {
		return getAddonJson().get("addonVersion").getAsString();
	}

	@Override
	public boolean isBeta() {
		return !getAddonJson().has("beta") || getAddonJson().get("beta").getAsBoolean();
	}

	@Override
	public float partialTicks() {
		return LabyMod.getInstance().getPartialTicks();
	}

	@Override
	public int chatButtonWidth() {
		return 0;
	}

	@Override
	public void notify(String title, String message, int ms) {
		LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("griefer_utils_icon", title, message);
	}

	@Override
	public void notifyError(String message) {
		notify("§c§lFehler ⚠", "§c" + message, 15_000);
	}

	@Override
	public void displayInChat(String message) {
		LabyMod.getInstance().displayMessageInChat(message);
	}

	@Override
	public void openWebsite(String url) {
		try {
			if (Platform.isLinux())
				Runtime.getRuntime().exec(new String[]{"xdg-open", url});
			else
				Desktop.getDesktop().browse(new URI(url));
		} catch (UnsupportedOperationException e) {
			BugReporter.reportError(Util.elevate(e, "Unsupported BROWSE for %s / %s / %s", Toolkit.getDefaultToolkit(), Platform.getOSType(), System.getProperty("os.name")));
		} catch (IOException | URISyntaxException e) {
			throw Util.elevate(e);
		}
	}

	@Override
	public boolean openFile(File file) {
		try {
			Desktop.getDesktop().open(file);
			return true;
		} catch (IOException e) {
			BugReporter.reportError(e);
			return false;
		}
	}

	@Override
	public void copyText(String text) {
		StringSelection sel = new StringSelection(text);
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		} catch (HeadlessException e) {
			notifyError("Keine Zwischenablage verfügbar!");
		} catch (IllegalStateException e) {
			notifyError("Die Zwischenablage wird derzeit verwendet!");
		}
	}

}
