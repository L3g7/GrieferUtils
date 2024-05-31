/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.events.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.addon.lifecycle.AddonPostEnableEvent;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.models.addon.info.InstalledAddonInfo;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static net.labymod.api.client.component.format.NamedTextColor.YELLOW;

@AddonMain
@ExclusiveTo(LABY_4)
public class Main {

	private static LoadedAddon addon;
	private static String addonDescription = YELLOW + "Der GrieferUtils-Server scheint nicht erreichbar zu sein :(";

	public static LoadedAddon getAddon() {
		if (addon == null)
			addon = Laby.labyAPI().addonService().getAddon(Main.class).orElseThrow();

		return addon;
	}

	@Subscribe
	public final void onAddonInitialize(AddonPostEnableEvent event) {
		System.out.println("GrieferUtils enabling");
		long begin = System.currentTimeMillis();

		try {
			Feature.getFeatures().forEach(Feature::init);

			EventRegisterer.init();
			Event.fire(OnEnable.class);
		} catch (RuntimeException e) {
			e.printStackTrace(System.err);
			throw e;
		}

		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
	}

	@EventListener
	private static void onWebData(WebDataReceiveEvent event) {
		// Load description from server, so it can be used as news board
		addonDescription = event.data.addonDescription;

		updateDescription();
	}

	@OnStartupComplete
	private static void updateDescription() {
		InstalledAddonInfo addon = getAddon().info();
		Reflection.set(addon, "author", "L3g7, L3g73 ┃ v" + labyBridge.addonVersion());
		Reflection.set(addon, "description", addonDescription);
	}

}
