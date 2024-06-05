/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.send;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class InteractablePlotSigns extends Feature {

	private static final Pattern PLOT_ID_PATTERN = Pattern.compile("ID: (-?\\d+);(-?\\d+)");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Interagierbare Plot-Schilder")
		.description("Führt bei Klick auf ein Plot-Schild /p i aus.")
		.icon("wooden_board");

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		TileEntity te = world().getTileEntity(event.packet.getPosition());

		if (!(te instanceof TileEntitySign))
			return;

		TileEntitySign tes = (TileEntitySign) te;
		if (tes.signText[0] == null)
			return;

		String txt = tes.signText[0].getUnformattedText();
		Matcher matcher = PLOT_ID_PATTERN.matcher(txt);
		if (!matcher.matches())
			return;

		send("/p i " + matcher.group(1) + ";" + matcher.group(2));
		event.cancel();
	}

}
