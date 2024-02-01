/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.balances;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.network.play.server.S3EPacketTeams;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;

@Singleton
public class CoinBalance extends Laby4Module {

	private static double coins = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kontostand")
		.description("Zeigt den Kontostand an.")
		.icon("coin_pile");

	@EventListener
	public void onPacket(PacketEvent.PacketReceiveEvent<S3EPacketTeams> event) {
		if (world() == null)
			return;

		if (!event.packet.getName().equals("money_value") || event.packet.getAction() != 2 || MinecraftUtil.getServerFromScoreboard().equals("Portal"))
			return;

		String money = event.packet.getPrefix();
		if (!money.endsWith("$")) // Still loading
			return;

		money = money.substring(0, money.length() - 1).replace(".", "").replace(",", ".");
		coins = Double.parseDouble(money);
	}

	@Override
	public String getValue() {
		if (coins == -1)
			return "?";

		return Constants.DECIMAL_FORMAT_98.format(coins) + "$";
	}

}
