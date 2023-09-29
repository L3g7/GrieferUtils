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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.ItemUseEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class SpawnerWithHeldItemFix extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spawner mit Item öffnen")
		.description("Ermöglicht das Öffnen von Spawnern auf öffentlichen Grundstücken, auch wenn man ein Item / einen Block in der Hand hält.")
		.icon(Material.MOB_SPAWNER);

	@EventListener
	private void onPacketSend(ItemUseEvent.Pre event) {
		if (event.stack == null || !ServerCheck.isOnGrieferGames())
			return;

		if (event.stack != player().getHeldItem())
			// Packet probably was sent by a mod / addon
			return;

		Block clickedBlock = world().getBlockState(event.pos).getBlock();
		if (clickedBlock != Blocks.mob_spawner)
			return;

		event.cancel();

		click(event.stack);
		mc().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(event.pos, event.side.getIndex(), null, event.hitX, event.hitY, event.hitZ));
		click(null);
	}

	private void click(ItemStack itemstack) {
		short transactionID = player().openContainer.getNextTransactionID(player().inventory);
		int slotId = player().inventory.currentItem + 36;
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(0, slotId, 0, 0, itemstack, transactionID));
	}

}
