/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.griefer_games.better_hopper;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.BlockEvent.BlockInteractEvent;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.ItemUseEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING;

@Singleton
public class BetterHopper extends Feature {

	static final SwitchSetting fillBoxes = SwitchSetting.create()
		.name("Anzeigeboxen füllen")
		.description("Ob die Boxen der Anzeige gefüllt werden sollen.")
		.defaultValue(true)
		.icon("color_palette");

	static final NumberSetting displayTime = NumberSetting.create()
		.name("Anzeigedauer")
		.description("Wie lange die optische Anzeige aktiv bleiben soll, in Sekunden.")
		.icon("hourglass")
		.defaultValue(10);

	static final SwitchSetting betterVisualisation = SwitchSetting.create()
		.name("Bessere optische Trichter-Anzeige")
		.description("Ersetzt die Partikel der optischen Trichter Anzeige durch Boxen / Linien.")
		.icon("lens")
		.subSettings(displayTime, fillBoxes);

	static final SwitchSetting showRange = SwitchSetting.create()
		.name("Trichterreichweite anzeigen")
		.description("Zeigt die Trichterreichweite an.")
		.icon("measurement");

	static final SwitchSetting showSourceHopper = SwitchSetting.create()
		.name("Ausgangstrichter anzeigen")
		.description("Zeigt beim Verbinden eines Trichters den Trichter an, von dem aus verbunden wird.")
		.icon("hopper");

	private static final NumberSetting lastHoppersLimit = NumberSetting.create()
		.name("Maximale Anzahl an Trichter")
		.description("Wie viele Trichter maximal angezeigt werden.")
		.icon("hopper")
		.min(1)
		.defaultValue(1);

	private static final SwitchSetting showLastHopper = SwitchSetting.create()
		.name("Letzte Trichter anzeigen")
		.description("Markiert die Trichter, die als letztes geöffnet wurden.")
		.icon("hopper")
		.subSettings(lastHoppersLimit);

	private static final SwitchSetting hopperWithHeldItemFix = SwitchSetting.create()
		.name("Trichter mit Item öffnen")
		.description("Ermöglicht das Öffnen von Trichtern, auch wenn man ein Item / einen Block in der Hand hält.")
		.icon("hopper")
		.since("2.4-BETA-1");

	private static final SwitchSetting sneakMode = SwitchSetting.create()
		.name("Sneak-Modus")
		.description("Öffnet bei Rechtsklicks immer die Einstellungen eines Trichters, auch wenn du nicht sneakst.")
		.icon("sneaking")
		.addHotkeySetting("den Sneak-Modus", null)
		.subSettings(hopperWithHeldItemFix);

	private static final SwitchSetting showFastTick = SwitchSetting.create()
		.name("Fast Tick Modus anzeigen")
		.description("Ersetzt die Diamant-Schuhe vom Fast Tick Modus mit gefärbter Wolle, damit es leichter zu erkennen ist.")
		.icon("lightning")
		.since("2.4-BETA-1");

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Trichteranzeige verbessern")
		.description("Verbessert die Anzeige von Trichtern.")
		.icon("hopper")
		.subSettings(betterVisualisation, showRange, showSourceHopper, showLastHopper, sneakMode, showFastTick);

	private static final List<BlockPos> lastClickedHoppers = new ArrayList<>();
	private int itemMoveOrigin = -1;

	public BetterHopper() {
		lastHoppersLimit.callback(i -> {
			while (i < lastClickedHoppers.size())
				lastClickedHoppers.remove(0);
		});
	}

	public static BetterHopper get() {
		return get(BetterHopper.class);
	}

	@EventListener
	private static void onBlockInteract(BlockInteractEvent event) {
		if (world().getBlockState(event.pos).getBlock() != Blocks.hopper)
			return;

		boolean isSneakModeEnabled = sneakMode.get() && enabled.get();

		if (player().getHeldItem() != null && (!isSneakModeEnabled || !hopperWithHeldItemFix.get()))
			return;

		if (player().isSneaking()) {
			addHopper(event.pos);
			return;
		}

		if (!isSneakModeEnabled)
			return;

		addHopper(event.pos);
		mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), START_SNEAKING));
		TickScheduler.runNextRenderTick(() -> mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), STOP_SNEAKING)));
	}

	private static void addHopper(BlockPos pos) {
		lastClickedHoppers.remove(pos);
		lastClickedHoppers.add(pos);
		if (lastClickedHoppers.size() > lastHoppersLimit.get())
			lastClickedHoppers.remove(0);
	}

	@EventListener
	private void onBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
		IBlockState state = event.packet.getBlockState();
		if (state == null || state.getBlock() != Blocks.hopper)
			lastClickedHoppers.remove(event.packet.getBlockPosition());
	}

	@EventListener
	private void onRenderTick(RenderWorldLastEvent event) {
		if (!showLastHopper.get() || lastClickedHoppers.isEmpty() || BetterHopperVisualisation.displayEnd >= System.currentTimeMillis())
			return;

		double color = 192 / (float) lastClickedHoppers.size();
		for (int i = 0; i < lastClickedHoppers.size(); ) {
			BlockPos lastClickedHopper = lastClickedHoppers.get(i);
			AxisAlignedBB bb = new AxisAlignedBB(lastClickedHopper, lastClickedHopper.add(1, 1, 1)).expand(0.001, 0.001, 0.001);
			RenderUtil.drawFilledBox(bb, new Color(0, (int) (++i * color) + 63, 0, 0x80), false);
		}
	}

	// Fast Tick

	@EventListener
	private void onGuiModify(GuiModifyItemsEvent event) {
		if (!showFastTick.get() || !event.getTitle().startsWith("§6Trichter-Einstellungen"))
			return;

		ItemStack fastTickItem = event.getItem(11);
		if (fastTickItem == null || fastTickItem.getItem() != Items.diamond_boots)
			return;

		boolean isEnabled = !EnchantmentHelper.getEnchantments(fastTickItem).isEmpty();
		fastTickItem.setItem(Item.getItemFromBlock(Blocks.wool));
		fastTickItem.setItemDamage(isEnabled ? 5 : 14);

		// Remove enchants
		EnchantmentHelper.setEnchantments(ImmutableMap.of(), fastTickItem);
	}

	// Hopper with held item fix

	@EventListener
	private void onItemUsePre(ItemUseEvent.Pre event) {
		if (!ServerCheck.isOnGrieferGames() || player().isSneaking())
			return;

		if (event.stack != player().getHeldItem())
			// Packet probably was sent by a mod / addon
			return;

		Block clickedBlock = world().getBlockState(event.pos).getBlock();
		if (clickedBlock != Blocks.hopper || MinecraftUtil.isInFarmwelt())
			return;

		if (!sneakMode.get() || !hopperWithHeldItemFix.get() || event.stack == null)
			return;

		if (FileProvider.getBridge(TempItemSaverBridge.class).isProtectedAgainstItemPickup(event.stack)) {
			labyBridge.notify("§cItemSaver", "§cDas Item in deiner Hand ist im ItemSaver!");
			return;
		}

		event.cancel();
		for (int i = 0; i < player().inventory.mainInventory.length; i++) {
			if (player().inventory.getStackInSlot(i) == null) {
				itemMoveOrigin = i;
				break;
			}
		}

		if (itemMoveOrigin == -1) {
			labyBridge.notify("§eTrichteranzeige verbessern", "§eDein Inventar ist voll!");
			return;
		}

		move(false);
		mc().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(event.pos, event.side.getIndex(), null, event.hitX, event.hitY, event.hitZ));
	}

	private void move(boolean toHotbar) {
		if (itemMoveOrigin < 9)
			itemMoveOrigin += 36;

		int hotbarSlot = player().inventory.currentItem;
		short transactionID = player().openContainer.getNextTransactionID(player().inventory);
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(0, itemMoveOrigin, hotbarSlot, 2, null, transactionID));

		if (itemMoveOrigin >= 36)
			itemMoveOrigin -= 36;

		if (!toHotbar) {
			ItemStack stack = player().inventory.getStackInSlot(hotbarSlot);
			player().inventory.setInventorySlotContents(itemMoveOrigin, stack);
			player().inventory.setInventorySlotContents(hotbarSlot, null);
			return;
		}

		ItemStack originStack = player().inventory.getStackInSlot(itemMoveOrigin);
		ItemStack slotStack = player().inventory.getStackInSlot(hotbarSlot);
		player().inventory.setInventorySlotContents(hotbarSlot, originStack);
		player().inventory.setInventorySlotContents(itemMoveOrigin, slotStack);
		itemMoveOrigin = -1;
	}

	@EventListener
	private void onGuiClose(PacketEvent.PacketSendEvent<C0DPacketCloseWindow> e) {
		TickScheduler.runNextRenderTick(() -> {
			if (itemMoveOrigin != -1)
				move(true);
		});
	}

}
