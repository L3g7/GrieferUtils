/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.ActionBar;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.commands.Commands;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.uncategorized.commands.Command.CommandBuilder.command;

@Singleton
public class GUGiveaway extends Feature {

	private static final Random RANDOM = new Random();

	private final SliderSetting rollDuration = SliderSetting.create()
		.name("Roll-Dauer (Sekunden)")
		.description("Wie lange die Roll-Animation in der Aktionsleiste läuft bevor der Gewinner feststeht.\nLängere Dauer = langsamere Animation.")
		.icon("clock")
		.min(5).max(15)
		.defaultValue(7);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("GU-Giveaway")
		.description("Startet per §e/gu:giveaway §7ein Giveaway unter allen GrieferUtils-Nutzern auf dem Server.\nZeigt eine Roll-Animation in der Aktionsleiste und verkündet den Gewinner im Title.")
		.icon("players")
		.since("2.5.0")
		.subSettings(rollDuration);

	@Override
	public void init() {
		super.init();
		Commands.registerCommand(command("giveaway").build(args -> startGiveaway()));
	}

	private void startGiveaway() {
		if (mc().theWorld == null || mc().getNetHandler() == null || mc().thePlayer == null) {
			display(ADDON_PREFIX + "§cNicht verbunden.");
			return;
		}

		UUID selfUUID = mc().thePlayer.getUniqueID();
		List<String> guUsers = new ArrayList<>();

		for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			UUID uuid = info.getGameProfile().getId();
			if (!uuid.equals(selfUUID) && Badges.isOnline(uuid))
				guUsers.add(info.getGameProfile().getName());
		}

		if (guUsers.isEmpty()) {
			display(ADDON_PREFIX + "§cKeine GrieferUtils-Nutzer auf dem Server gefunden.");
			return;
		}

		String winner = guUsers.get(RANDOM.nextInt(guUsers.size()));
		int duration = rollDuration.get();
		int totalTicks = duration * 20;
		int minDelay = 2;
		int maxDelay = duration * 2;

		// Build frame schedule: starts dense, slows down toward end
		List<Integer> frameTicks = new ArrayList<>();
		int t = 0;
		while (t < totalTicks) {
			frameTicks.add(t);
			double progress = (double) t / totalTicks;
			int delay = minDelay + (int) (progress * (maxDelay - minDelay));
			t += Math.max(minDelay, delay);
		}
		frameTicks.add(totalTicks);

		for (int i = 0; i < frameTicks.size(); i++) {
			boolean isLast = i == frameTicks.size() - 1;
			String displayName = isLast ? winner : guUsers.get(RANDOM.nextInt(guUsers.size()));
			int tick = frameTicks.get(i);

			TickScheduler.runAfterClientTicks(() -> {
				if (isLast) {
					ActionBar.set(null);
					mc().ingameGUI.displayTitle("§6GU-Giveaway", null, -1, -1, -1);
					mc().ingameGUI.displayTitle(null, "§a✨ " + winner + " §a✨", -1, -1, -1);
					mc().ingameGUI.displayTitle(null, null, 5, 60, 20);
				} else {
					ActionBar.set("§6[GU-Giveaway] §e🎰 " + displayName);
				}
			}, tick);
		}
	}

}
