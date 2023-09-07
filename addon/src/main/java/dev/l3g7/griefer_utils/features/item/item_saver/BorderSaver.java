/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.BlockInteractEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.features.world.ItemSearch;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;

import static dev.l3g7.griefer_utils.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;
import static net.labymod.ingamegui.Module.mc;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK;
import static net.minecraft.util.EnumFacing.UP;

/**
 * Suppresses left clicks and dropping when holing a diamond sword enchanted with looting 21.
 */
@Singleton
public class BorderSaver extends ItemSaver {

	private static final int ACCEPT_SLOT_ID = 11, PREVIEW_SLOT_ID = 13, DECLINE_SLOT_ID = 15;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Rand-Saver")
		.description("Fragt beim Einlösen eines Randes nach einer Bestätigung.")
		.icon(createItem(Blocks.obsidian, 0, true));

	private final IInventory inv = new InventoryBasic(ItemSearch.marker + "§0Willst du den Rand einlösen?", false, 27);

	public BorderSaver() {
		ItemStack grayGlassPane = createItem(Blocks.stained_glass_pane, 7, "§8");

		// Fill inventory with gray glass panes
		for (int slot = 0; slot < 27; slot++)
			inv.setInventorySlotContents(slot, grayGlassPane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID, createItem(Items.dye, 10, "§aEinlösen"));
		inv.setInventorySlotContents(PREVIEW_SLOT_ID, createItem(Items.ender_eye, 0, "§3Vorschau anzeigen"));
		inv.setInventorySlotContents(DECLINE_SLOT_ID, createItem(Items.dye, 1, "§cAbbrechen"));
	}

	@EventListener
	public void onPacket(PacketEvent.PacketSendEvent<C07PacketPlayerDigging> event) {
		if (!isEnabled())
			return;

		C07PacketPlayerDigging.Action action = event.packet.getStatus();
		if (action == C07PacketPlayerDigging.Action.DROP_ITEM || action == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS)
			return;

		if (isHoldingBorder()) {
			event.cancel();
			// Re-sending the original packet doesn't work, for some reason
			displayScreen(() -> mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(START_DESTROY_BLOCK, new BlockPos(player()), UP)));
		}
	}

	@EventListener
	public void onPlayerInteract(BlockInteractEvent event) {
		if (!isEnabled() || !isHoldingBorder())
			return;

		event.cancel();
		displayScreen(() -> mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem()));
	}

	private boolean isHoldingBorder() {
		ItemStack heldItem = player().getHeldItem();

		if (heldItem == null || !heldItem.hasTagCompound())
			return false;

		// Check if it's a border
		return heldItem.getTagCompound().getBoolean("wall_effect");
	}

	private void displayScreen(Runnable callback) {
		mc().displayGuiScreen(new GuiChest(player().inventory, inv) {

			{
				inventorySlots.windowId = -1337;
			}

			protected void handleMouseClick(Slot slot, int slotId, int btn, int type) {
				if (slot != null)
					slotId = slot.slotNumber;

				if (slotId == DECLINE_SLOT_ID)
					mc.thePlayer.closeScreenAndDropStack();

				if (slotId == PREVIEW_SLOT_ID) {
					send("/rand test");
					mc.thePlayer.closeScreenAndDropStack();
				}

				if (slotId != ACCEPT_SLOT_ID)
					return;

				callback.run();
				mc.displayGuiScreen(null);
			}

		});
	}

}
