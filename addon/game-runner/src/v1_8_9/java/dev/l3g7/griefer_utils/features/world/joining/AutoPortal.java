/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.joining;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.settings.types.CitybuildSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

import static dev.l3g7.griefer_utils.core.api.misc.os.OS.OS;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.send;

/**
 * Automatically enters the portal room when joining.
 */
@Singleton
public class AutoPortal extends Feature {

	private boolean joined = false;

	private final CitybuildSetting citybuild = CitybuildSetting.create()
		.name("Citybuild")
		.description("Welcher Citybuild betreten werden soll.");

	private final SwitchSetting join = SwitchSetting.create()
		.name("/portal beim Start")
		.description("Betritt automatisch GrieferGames, sobald Minecraft gestartet wurde.")
		.icon("portal");

	private final SwitchSetting maximize = SwitchSetting.create()
		.name("Automatisch maximieren")
		.description("Ob Minecraft nach dem Starten automatisch maximiert werden soll.")
		.icon("glass_pane");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch /portal")
		.description("Betritt automatisch den Portalraum.")
		.icon("portal")
		.subSettings(citybuild, join, maximize);

	public void init() {
		super.init();
		if (OS.isFallback())
			maximize.name("§c§o§m" + maximize.name())
				.description("§c§oMaximierung ist für " + System.getProperty("os.name") + " nicht implementiert.")
				.callback(v -> { if (v) maximize.set(false); });
	}

	@EventListener(priority = Priority.HIGH)
	public void onServerJoin(ServerEvent.GrieferGamesJoinEvent event) {
		if (citybuild.get() == Citybuild.ANY) {
			joined = true;
			send("/portal");
			return;
		}

		joined = false;

		TickScheduler.runAfterClientTicks(() -> {
			if (!joined)
				send("/switch " + citybuild.get().getInternalName());

			joined = true;
		}, 20);
	}

	@EventListener
	public void onChatMessage(MessageReceiveEvent event) {
		if (!joined && event.message.getUnformattedText().equals("[GGAuth] Du wurdest erfolgreich verifiziert.")) {
			joined = true;
			send("/switch " + citybuild.get().getInternalName());
		}
	}

	@OnStartupComplete
	public void onStartupComplete() {
		if (!isEnabled())
			return;

		if (join.get())
			TickScheduler.runNextClientTick(() -> mc().displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc(), new ServerData("GrieferGames", "griefergames.net", false))));

		if (maximize.get())
			TickScheduler.runNextClientTick(OS::maximizeWindow);
	}

}
