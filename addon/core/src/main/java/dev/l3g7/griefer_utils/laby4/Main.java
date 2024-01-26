/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.addon.lifecycle.AddonPostEnableEvent;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class Main {

	private static LoadedAddon addon;

	public static LoadedAddon getAddon() {
		if (addon == null)
			addon = Laby.labyAPI().addonService().getAddon(Main.class).orElseThrow();

		return addon;
	}

	@Subscribe
	public final void onAddonInitialize(AddonPostEnableEvent event) {
		System.out.println("GrieferUtils enabling");
		long begin = System.currentTimeMillis();

		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (meta.isAbstract())
				return;

			Feature instance = FileProvider.getSingleton(meta.load());
			instance.init();
		});

		EventRegisterer.init();
		Event.fire(OnEnable.class);

		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
	}

}
