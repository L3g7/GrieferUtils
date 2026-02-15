/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class BlockOfTheDayCounter extends SimpleWidget {

	private static int botdFound = 0;

	private final SwitchSetting showPopup = SwitchSetting.create()
		.name("Popup anzeigen")
		.description("Zeigt ein Popup an, wenn ein Block des Tages gefunden wurde.")
		.icon("bell");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Block des Tages-Zähler")
		.description("Zählt, wie oft Block des Tages gefunden wurde.")
		.icon("brick")
		.subSettings(showPopup);

	public static void onBotd() {
		BlockOfTheDayCounter counter = FileProvider.getSingleton(BlockOfTheDayCounter.class);
		if (!counter.isEnabled())
			return;

		botdFound++;
		Config.set(getPath(), new JsonPrimitive(botdFound));
		Config.save();
		if (!counter.showPopup.get())
			return;

		mc().ingameGUI.displayTitle("§aBlock des Tages", null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, "§fDu hast einen §aBlock des Tages §fgefunden!", -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 50, 10);
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadBalance(ServerEvent.GrieferGamesJoinEvent ignored) {
		if (Config.has(getPath()))
			botdFound = Config.get(getPath()).getAsInt();
	}

	private static String getPath() {
		return "modules.block_of_the_day_counter." + mc().getSession().getProfile().getId() + ".";
	}

	@Override
	public String getValue() {
		return String.valueOf(botdFound);
	}

}
