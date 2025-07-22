/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.bsf.data.BSFSearchable;
import dev.l3g7.griefer_utils.features.world.bsf.gui.GuiBSF;

import java.util.*;

@Singleton
public class BSF extends Feature {

	public static final Map<Citybuild, Map<BSFSearchable, List<SearchData>>> SEARCH_DATA = new HashMap<>();
	public static final Set<String> readyCbs = Collections.synchronizedSet(new HashSet<>());
	private static boolean requestedOnJoin = false;

	public static boolean notify = false;

	private final KeySetting setting = KeySetting.create()
		.name("Gui öffnen")
		.icon("key")
		.description("Die Taste, mit der das Gui geöffnet werden soll.")
		.pressCallback(b -> { if (b) GuiBSF.GUI.open(); });

	@MainElement
	private final CategorySetting button = CategorySetting.create()
		.name("Biom- und Strukturen-Suche")
		.icon("earth")
		.description("Ermöglicht das Suchen von Biomen und Strukturen in der Farmwelt.")
		.subSettings(setting, HeaderSetting.create(),
			HeaderSetting.create("Das Gui lässt sich auch mit /bss öffnen.")
				.center());

	public static boolean hasData() {
		return readyCbs.contains(MinecraftUtil.getCurrentCitybuild().getInternalName());
	}

	public static boolean isInFarmwelt() {
		return BSFCollector.isInFarmwelt();
	}

	public static void triggerNotification() {
		if (BSF.notify) {
			LabyBridge.labyBridge.notify("§aSuche ist nun bereit!", "§aDie Biom- und Strukturen-Suche\nkann nun verwendet werden.");
			notify = false;
		}
	}

	@EventListener
	private static void onGGJoin(GrieferGamesJoinEvent event) {
		if (requestedOnJoin)
			return;

		requestedOnJoin = true;
		GuiBSF.lastUpdate = System.currentTimeMillis();
		GUServer.getBSFReady().thenAccept(BSF.readyCbs::addAll);
	}

	@EventListener
	private static void onMessageSend(MessageEvent.MessageSendEvent event ) {
		if (event.message.toLowerCase().startsWith("/bss")) {
			event.cancel();
			TickScheduler.runAfterRenderTicks(() -> GuiBSF.GUI.open(), 1);
		}

	}

	public record SearchData(int x, int z, int index) {}

}
