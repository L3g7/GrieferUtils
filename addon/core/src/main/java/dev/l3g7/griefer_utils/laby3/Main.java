/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
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

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.laby3.bridges.Laby3MinecraftBridge.laby3MinecraftBridge;

/**
 * The main class.
 */
public class Main extends LabyModAddon {

	private static Main instance;

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

		Feature.getFeatures().forEach(Feature::init);

		EventRegisterer.init();
		Event.fire(OnEnable.class);

		Map<String, DynamicModTexture> map = LabyMod.getInstance().getDynamicTextureManager().getResourceLocations();
		map.put("griefer_utils_icon", laby3MinecraftBridge.createDynamicTexture("griefer_utils/icons/icon.png", "griefer_utils_icon"));

		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
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
