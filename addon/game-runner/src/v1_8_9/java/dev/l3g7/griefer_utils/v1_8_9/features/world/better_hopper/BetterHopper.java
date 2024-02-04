/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world.better_hopper;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockEvent.BlockInteractEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import dev.l3g7.griefer_utils.v1_8_9.util.render.RenderUtil;
import net.labymod.utils.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING;

@Singleton
public class BetterHopper extends Feature {

	static final SwitchSetting fillBoxes = SwitchSetting.create()
		.name("Anzeigeboxen füllen")
		.description("Ob die Boxen der Anzeige gefüllt werden sollen.")
		.defaultValue(true)
		.icon(ItemUtil.createItem(Blocks.wool, 14, false));

	static final NumberSetting displayTime = NumberSetting.create()
		.name("Anzeigedauer")
		.description("Wie lange die optische Anzeige aktiv bleiben soll, in Sekunden.")
		.icon("hourglass")
		.defaultValue(10);

	static final SwitchSetting betterVisualisation = SwitchSetting.create()
		.name("Bessere optische Trichter-Anzeige")
		.description("Ersetzt die Partikel der optischen Trichter Anzeige durch Boxen / Linien.")
		.icon(Items.ender_eye)
		.subSettings(displayTime, fillBoxes);

	static final SwitchSetting showRange = SwitchSetting.create()
		.name("Trichterreichweite anzeigen")
		.description("Zeigt die Trichterreichweite an.")
		.icon("ruler");

	static final SwitchSetting showSourceHopper = SwitchSetting.create()
		.name("Ausgangstrichter anzeigen")
		.description("Zeigt beim Verbinden eines Trichters den Trichter an, von dem aus verbunden wird.")
		.icon(Blocks.hopper);

	private static final NumberSetting lastHoppersLimit = NumberSetting.create()
		.name("Maximale Anzahl an Trichter")
		.description("Wie viele Trichter maximal angezeigt werden.")
		.icon(Blocks.hopper)
		.min(1)
		.defaultValue(1);

	private static final SwitchSetting showLastHopper = SwitchSetting.create()
		.name("Letzte Trichter anzeigen")
		.description("Markiert die Trichter, die als letztes geöffnet wurden.")
		.icon(Blocks.hopper)
		.subSettings(lastHoppersLimit);

	private static final SwitchSetting sneakMode = SwitchSetting.create()
		.name("Sneak-Modus")
		.description("Öffnet bei Rechtsklicks immer die Einstellungen eines Trichters, auch wenn du nicht sneakst.")
		.icon("sneaking")
		.addHotkeySetting("den Sneak-Modus", null);

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Trichteranzeige verbessern")
		.description("Verbessert die Anzeige von Trichtern.")
		.icon(Blocks.hopper)
		.subSettings(betterVisualisation, showRange, showSourceHopper, showLastHopper, sneakMode);

	private static final List<BlockPos> lastClickedHoppers = new ArrayList<>();

	public BetterHopper() {
		lastHoppersLimit.callback(i -> {
			lastHoppersLimit.icon(new ItemStack(Blocks.hopper, i));
			while (i < lastClickedHoppers.size())
				lastClickedHoppers.remove(0);
		});
	}

	@EventListener
	private static void onBlockInteract(BlockInteractEvent event) {
		if (world().getBlockState(event.pos).getBlock() != Blocks.hopper || player().getHeldItem() != null)
			return;

		if (player().isSneaking()) {
			addHopper(event.pos);
			return;
		}

		if (!sneakMode.get() || !enabled.get())
			return;

		addHopper(event.pos);
		mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), START_SNEAKING));
		TickScheduler.runAfterRenderTicks(() -> mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), STOP_SNEAKING)), 1);
	}

	private static void addHopper(BlockPos pos) {
		lastClickedHoppers.remove(pos);
		lastClickedHoppers.add(pos);
		if (lastClickedHoppers.size() > lastHoppersLimit.get())
			lastClickedHoppers.remove(0);
	}

	@EventListener
	private void onBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
		if (event.packet.getBlockState().getBlock() != Blocks.hopper)
			lastClickedHoppers.remove(event.packet.getBlockPosition());
	}

	@EventListener
	private void onRenderTick(RenderWorldLastEvent event) {
		if (!showLastHopper.get() || lastClickedHoppers.isEmpty())
			return;

		double color = 192 / (float) lastClickedHoppers.size();
		for (int i = 0; i < lastClickedHoppers.size();) {
			BlockPos lastClickedHopper = lastClickedHoppers.get(i);
			AxisAlignedBB bb = new AxisAlignedBB(lastClickedHopper, lastClickedHopper.add(1, 1, 1)).expand(0.001, 0.001, 0.001);
			RenderUtil.drawFilledBox(bb, new Color(0, (int) (++i * color) + 63, 0, 0x80), false);
		}
	}

}
