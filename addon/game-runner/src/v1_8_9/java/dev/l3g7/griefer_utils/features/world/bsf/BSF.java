/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.world.bsf.data.BSFSearchable;
import dev.l3g7.griefer_utils.features.world.bsf.gui.GuiBSF;

import java.util.*;

public class BSF {

	public static final Map<Citybuild, Map<BSFSearchable, List<SearchData>>> SEARCH_DATA = new HashMap<>();
	public static final Set<String> readyCbs = Collections.synchronizedSet(new HashSet<>());
	private static boolean requestedOnJoin = false;

	public static boolean hasData() {
		return readyCbs.contains(MinecraftUtil.getCurrentCitybuild().getInternalName());
	}

	public static boolean isInFarmwelt() {
		return BSFCollector.isInFarmwelt();
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
		if (event.message.toLowerCase().startsWith("/bsf")) {
			event.cancel();
			TickScheduler.runAfterRenderTicks(() -> new GuiBSF().open(), 1);
		}
	}

	public static class SearchData {

		public final int x;
		public final int z;
		public final int index;

		public SearchData(int x, int z, int index) {
			this.x = x;
			this.z = z;
			this.index = index;
		}
	}

}
