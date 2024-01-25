/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.BlockEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.features.world.ItemSearch;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

import static dev.l3g7.griefer_utils.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class ParticleSaver extends ItemSaver {

	private static final int ACCEPT_SLOT_ID = 12, DECLINE_SLOT_ID = 14;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Partikel-Saver")
		.description("Fragt beim Einlösen eines Partikel-Effekts nach einer Bestätigung.")
		.icon(createItem(Items.dye, 10, true));

	private final IInventory inv = new InventoryBasic(ItemSearch.marker + "§0Willst du den Effekt einlösen?", false, 27);

	public ParticleSaver() {
		ItemStack grayGlassPane = createItem(Blocks.stained_glass_pane, 7, "§8");

		// Fill inventory with gray glass panes
		for (int slot = 0; slot < 27; slot++)
			inv.setInventorySlotContents(slot, grayGlassPane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID, createItem(Items.dye, 10, "§aEinlösen"));
		inv.setInventorySlotContents(DECLINE_SLOT_ID, createItem(Items.dye, 1, "§cAbbrechen"));
	}

	@EventListener
	public void onPacket(PacketEvent.PacketSendEvent<C07PacketPlayerDigging> event) {
		C07PacketPlayerDigging.Action action = event.packet.getStatus();
		if (action == C07PacketPlayerDigging.Action.DROP_ITEM || action == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS)
			return;

		if (isHoldingParticle()) {
			event.cancel();
			displayScreen();
		}
	}

	@EventListener
	public void onPlayerInteract(BlockEvent.BlockInteractEvent event) {
		if (!isHoldingParticle())
			return;

		event.cancel();
		displayScreen();
	}

	private boolean isHoldingParticle() {
		return "§7Wird mit §e/deleteparticle §7zerstört.".equals(ItemUtil.getLastLore(MinecraftUtil.mc().thePlayer.getHeldItem()));
	}

	private void displayScreen() {
		MinecraftUtil.mc().displayGuiScreen(new GuiChest(player().inventory, inv) {

			{
				inventorySlots.windowId = -1337;
			}

			protected void handleMouseClick(Slot slot, int slotId, int btn, int type) {
				if (slot != null)
					slotId = slot.slotNumber;

				if (slotId == DECLINE_SLOT_ID)
					mc.thePlayer.closeScreenAndDropStack();

				if (slotId != ACCEPT_SLOT_ID)
					return;

				mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
				mc.displayGuiScreen(null);
			}

		});
	}

}
