/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockInteractEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.v1_8_9.features.world.ItemSearch;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

import static dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class ParticleSaver extends ItemSaver {

	private static final int ACCEPT_SLOT_ID = 12, DECLINE_SLOT_ID = 14;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
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
	public void onPlayerInteract(BlockInteractEvent event) {
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
