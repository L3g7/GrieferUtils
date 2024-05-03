/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.events.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.laby3.events.LabyModAddonsGuiOpenEvent;
import dev.l3g7.griefer_utils.laby3.settings.MainPage;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.texture.DynamicModTexture;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.laby3.bridges.Laby3MinecraftBridge.laby3MinecraftBridge;
import static net.labymod.utils.ModColor.YELLOW;

/**
 * The main class.
 */
@ExclusiveTo(LABY_3)
public class Main extends LabyModAddon {

	private static Main instance;
	private static String addonDescription = YELLOW + "Der GrieferUtils-Server scheint nicht erreichbar zu sein :(";

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
	}

	@Override
	public void onEnable() {
		System.out.println("GrieferUtils enabling");
		long begin = System.currentTimeMillis();

		// Initialize addon
		Feature.getFeatures().forEach(Feature::init);
		EventRegisterer.init();
		Event.fire(OnEnable.class);

		// Fix icon
		Map<String, DynamicModTexture> map = LabyMod.getInstance().getDynamicTextureManager().getResourceLocations();
		map.put("griefer_utils_icon", laby3MinecraftBridge.createDynamicTexture("griefer_utils/icons/icon.png", "griefer_utils_icon"));

		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
	}

	@EventListener
	private static void onWebData(WebDataReceiveEvent event) {
		addonDescription = event.data.addonDescription;
		updateDescription();
	}

	@OnStartupComplete
	private static void updateDescription() {
		AddonInfoManager addonInfoManager = AddonInfoManager.getInstance();
		addonInfoManager.init();

		UUID addonUuid = instance.about.uuid;

		// Retrieve addonInfo
		AddonInfo addonInfo = addonInfoManager.getAddonInfoMap().get(addonUuid);
		if (addonInfo == null)
			addonInfo = AddonLoader.getOfflineAddons().stream().filter(addon -> addon.getUuid().equals(addonUuid)).findFirst().orElse(null);

		if (addonInfo == null)
			return;

		// Update description
		Reflection.set(addonInfo, "author", "L3g7, L3g73 \u2503 v" + labyBridge.addonVersion());
		Reflection.set(addonInfo, "description", addonDescription);
	}

	/**
	 * Ensures GrieferUtils is shown in the {@link LabyModAddonsGui}.
	 */
	@EventListener
	private static void onGuiOpen(LabyModAddonsGuiOpenEvent event) {
		UUID uuid = Main.getInstance().about.uuid;
		for (AddonInfo addonInfo : AddonInfoManager.getInstance().getAddonInfoList())
			if (addonInfo.getUuid().equals(uuid))
				return;

		for (AddonInfo offlineAddon : AddonLoader.getOfflineAddons()) {
			if (offlineAddon.getUuid().equals(uuid)) {
				AddonInfoManager.getInstance().getAddonInfoList().add(offlineAddon);
				return;
			}
		}

		throw new RuntimeException("GrieferUtils couldn't be loaded");
	}

	@Override
	public void loadConfig() {}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		list.addAll(c(MainPage.collectSettings()));
	}

}
