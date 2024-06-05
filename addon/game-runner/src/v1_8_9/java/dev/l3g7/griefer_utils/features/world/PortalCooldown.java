/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class PortalCooldown extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Portal-Cooldown")
		.description("Zeigt dir den 12s-Cooldown beim Betreten des Portalraums in der XP-Leiste an.")
		.icon("hourglass");

	private long timeoutEnd = -1;

	@EventListener(triggerWhenDisabled = true)
	public void onMessage(MessageReceiveEvent event) {
		String msg = event.message.getFormattedText();

		if (msg.equals("§r§8[§r§6GrieferGames§r§8] §r§fDu bist im §r§5Portalraum§r§f. Wähle deinen Citybuild aus.§r")) {
			timeoutEnd = System.currentTimeMillis() + 12_000;
		}

		if (!MinecraftUtil.getServerFromScoreboard().equals("Portal"))
			return;

		if ((msg.startsWith("§r§cKicked whilst connecting") && !msg.contains("Du hast dich zu schnell wieder eingeloggt."))
			|| msg.startsWith("§r§cCould not connect to a default or fallback server")
			|| msg.startsWith("§c§r§cKein Verbindungsaufbau zu ")
			|| msg.startsWith("§c§r§cUnable to connect to ")) {
			timeoutEnd = System.currentTimeMillis() + 12_000;
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent event) {
		timeoutEnd = -1;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerQuit(ServerQuitEvent event) {
		timeoutEnd = -1;
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (timeoutEnd < 0)
			return;

		long diff = timeoutEnd - System.currentTimeMillis();

		diff = Math.max(diff, 0);

		player().experienceLevel = (int) Math.ceil(diff / 1000f);
		player().experience = diff / 12_000f;
	}

}