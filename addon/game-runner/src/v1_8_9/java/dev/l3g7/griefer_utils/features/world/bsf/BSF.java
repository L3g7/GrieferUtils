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
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class BSF extends Feature {

	public static final Map<Citybuild, Map<BSFSearchable, List<SearchData>>> SEARCH_DATA = new HashMap<>();
	public static final Set<Citybuild> READY_CBS = Collections.synchronizedSet(new HashSet<>());
	private static boolean requestedOnJoin = false;
	private static Calendar nextReset = getNextFarmweltReset();

	public static Set<Citybuild> notify = new HashSet<>();

	private final KeySetting setting = KeySetting.create()
		.name("Gui öffnen")
		.icon("key")
		.description("Die Taste, mit der das Gui geöffnet werden soll.")
		.pressCallback(b -> { if (b) new GuiBSF().open(); });

	@MainElement
	private final CategorySetting button = CategorySetting.create()
		.name("Biom- und Strukturen-Suche")
		.icon("earth")
		.description("Ermöglicht das Suchen von Biomen und Strukturen in der Farmwelt.")
		.subSettings(setting, HeaderSetting.create(),
			HeaderSetting.create("Das Gui lässt sich auch mit /bss öffnen.")
				.center());

	public static boolean hasData() {
		if (nextReset.before(Calendar.getInstance())) {
			nextReset = getNextFarmweltReset();
			READY_CBS.clear();
			SEARCH_DATA.clear();
		}

		return READY_CBS.contains(MinecraftUtil.getCurrentCitybuild());
	}

	public static boolean isInFarmwelt() {
		return BSFCollector.isInFarmwelt();
	}

	public static void updateCBs(List<String> internalCbs) {
		List<Citybuild> cbs = internalCbs.stream().map(Citybuild::getCitybuild).collect(Collectors.toList());

		READY_CBS.clear();
		READY_CBS.addAll(cbs);

		List<Citybuild> notifyCbs = cbs.stream().filter(notify::contains).collect(Collectors.toList());
		if (notifyCbs.isEmpty())
			return;

		notifyCbs.forEach(notify::remove);
		StringBuilder msg = new StringBuilder("§a");
		if (notifyCbs.size() == 1) {
			msg.append(formatCB(notifyCbs.remove(0)));
			msg.append(" ist nun bereit!");
		} else {
			Citybuild last = notifyCbs.remove(notifyCbs.size() - 1);
			msg.append(notifyCbs.stream().map(BSF::formatCB).collect(Collectors.joining(", ")));
			msg.append(" & ").append(formatCB(last));
			msg.append(" sind nun bereit!");
		}

		LabyBridge.labyBridge.notify(msg.toString(), "§aDie Biom- und Strukturen-Suche\nkann dort nun verwendet werden.");

		if (notifyCbs.contains(MinecraftUtil.getCurrentCitybuild()) && mc().currentScreen instanceof GuiBSF)
			new GuiBSF().open();
	}

	private static String formatCB(Citybuild cb) {
		return cb.getName().replace("Citybuild ", "CB");
	}

	@EventListener
	private static void onGGJoin(GrieferGamesJoinEvent event) {
		if (requestedOnJoin)
			return;

		requestedOnJoin = true;
		GuiBSF.lastUpdate = System.currentTimeMillis();
		GUServer.getBSFReady().thenAccept(BSF::updateCBs);
	}

	@EventListener
	private static void onMessageSend(MessageEvent.MessageSendEvent event ) {
		if (event.message.toLowerCase().startsWith("/bss")) {
			event.cancel();
			TickScheduler.runNextRenderTick(() -> new GuiBSF().open());
		}

	}

	private static Calendar getNextFarmweltReset() {
		Calendar reset = Calendar.getInstance();

		reset.set(Calendar.DAY_OF_MONTH, 4);
		reset.set(Calendar.HOUR_OF_DAY, 4);
		reset.set(Calendar.MINUTE, 0);
		reset.set(Calendar.SECOND, 0);

		if (reset.before(Calendar.getInstance()))
			reset.add(Calendar.MONTH, 1);

		return reset;
	}

	public record SearchData(int x, int z, int index) {}

}
