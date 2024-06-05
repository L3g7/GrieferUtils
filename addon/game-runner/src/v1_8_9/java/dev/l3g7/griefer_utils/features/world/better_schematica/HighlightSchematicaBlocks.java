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

package dev.l3g7.griefer_utils.v1_8_9.features.world.better_schematica;

import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IWorldAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.core.util.SchematicaUtil.getWorld;

public class HighlightSchematicaBlocks {

	private static final Map<Item, List<ItemStack>> requiredItems = new HashMap<>();
	private static ISchematic schematic = null;
	private static ItemStack heldItem = null;
	private static boolean placedCompressedBlock = false;

	private static boolean triggeredFromBlockUpdate = false;
	private static final IWorldAccess WORLD_ACCESS = new IWorldAccess() {

		public void markBlockForUpdate(BlockPos pos) {
			if (triggeredFromBlockUpdate)
				return;

			triggeredFromBlockUpdate = true;
			TickScheduler.runAfterRenderTicks(() -> {
				updateItemList();
				triggeredFromBlockUpdate = false;
			}, 1);
		}

		public void notifyLightSet(BlockPos pos) {}
		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}
		public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {}
		public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {}
		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {}
		public void onEntityAdded(Entity entityIn) {}
		public void onEntityRemoved(Entity entityIn) {}
		public void playRecord(String recordName, BlockPos blockPosIn) {}
		public void broadcastSound(int soundID, BlockPos pos, int data) {}
		public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data) {}
		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
	};

	@SuppressWarnings("unused")
	public static void drawCuboid(WorldRenderer worldRenderer, BlockPos pos, int sides, int argb) {
		GeometryTessellator.drawCuboid(worldRenderer, pos, sides, isHoldingRequiredItem(pos) ? 0x7F00FF00 : 0x3F000000 | argb);
	}

	private static boolean isHoldingRequiredItem(BlockPos pos) {
		if (!BetterSchematica.isHighlightBlocksEnabled())
			return false;

		Block block = getWorld().getBlockState(pos).getBlock();
		if (block == Blocks.air)
			return false;

		ItemStack stack = getPickBlock(block, getWorld(), pos);
		if (stack == null)
			return false;

		return stack.isItemEqual(heldItem);
	}

	static void onRenderTick(RenderTickEvent event) {
		if (player() == null)
			return;

		if (getWorld() == null) {
			schematic = null;
			requiredItems.clear();
			return;
		}

		if (SchematicaUtil.getSchematic() != schematic) {
			schematic = SchematicaUtil.getSchematic();
			updateItemList();
			getWorld().removeWorldAccess(WORLD_ACCESS);
			getWorld().addWorldAccess(WORLD_ACCESS);
		}

		if (placedCompressedBlock) {
			if (player().getHeldItem() != null)
				placedCompressedBlock = false;

			return;
		}

		if (heldItem == null && player().getHeldItem() == null || player().getHeldItem() == heldItem)
			return;

		// Switched from air to a block or the other way around
		if (heldItem == null || player().getHeldItem() == null) {
			boolean refresh = isHeldItemRequired(heldItem) || isHeldItemRequired(player().getHeldItem());
			heldItem = player().getHeldItem();
			if (refresh)
				RenderSchematic.INSTANCE.refresh();

			return;
		}

		if (heldItem.isItemEqual(player().getHeldItem()) || (!isHeldItemRequired(heldItem) && !isHeldItemRequired(player().getHeldItem()))) {
			heldItem = player().getHeldItem();
			return;
		}

		heldItem = player().getHeldItem();
		RenderSchematic.INSTANCE.refresh();
	}

	static void onPacketSend(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		ItemStack stack = event.packet.getStack();
		if (event.packet.getPlacedBlockDirection() == 255 || stack == null)
			return;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("stackSize"))
			return;

		if (!isHeldItemRequired(stack))
			return;

		if (stack.stackSize == 1 && ItemUtil.getDecompressedAmount(stack) == 1)
			return;

		placedCompressedBlock = true;
		heldItem = stack;
	}

	private static boolean isHeldItemRequired(ItemStack heldItem) {
		if (heldItem == null)
			return false;

		if (!requiredItems.containsKey(heldItem.getItem()))
			return false;

		for (ItemStack itemStack : requiredItems.get(heldItem.getItem()))
			if (itemStack.isItemEqual(heldItem))
				return true;

		return false;
	}

	private static void updateItemList() {
		requiredItems.clear();

		for (int x = 0; x < schematic.getWidth(); x++) {
			for (int y = 0; y < schematic.getHeight(); y++) {
				for (int z = 0; z < schematic.getLength(); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					Block block = schematic.getBlockState(pos).getBlock();
					if (block == Blocks.air)
						continue;

					ItemStack stack;
					try {
						stack = getPickBlock(block, getWorld(), pos);
					} catch (Throwable t) {
						System.err.println(t.getMessage());
						// idk why this happens (Cannot get property [...] as is does not exist in minecraft:air)
						return;
					}

					if (stack == null)
						continue;

					BlockPos worldPos = SchematicaUtil.getPosition().add(pos);
					Block worldBlock = world().getBlockState(worldPos).getBlock();
					ItemStack worldStack = getPickBlock(worldBlock, world(), worldPos);
					if (stack.isItemEqual(worldStack)) // Required block is already placed
						continue;

					requiredItems.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
				}
			}
		}
	}

}
