/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.world.better_schematica;

import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class HighlightSchematicaBlocks {

	private static final Map<Item, List<ItemStack>> requiredItems = new HashMap<>();
	private static ISchematic schematic = null;
	private static ItemStack heldItem = null;

	@SuppressWarnings("unused")
	public static void drawCuboid(WorldRenderer worldRenderer, BlockPos pos, int sides, int argb) {
		GeometryTessellator.drawCuboid(worldRenderer, pos, sides, isHoldingRequiredItem(pos) ? 0x7F00FF00 : 0x3F000000 | argb);
	}

	private static boolean isHoldingRequiredItem(BlockPos pos) {
		if (!BetterSchematica.isHighlightBlocksEnabled())
			return false;

		Block block = SchematicaUtil.getWorld().getBlockState(pos).getBlock();
		if (block == Blocks.air)
			return false;

		ItemStack stack = block.getPickBlock(mc().objectMouseOver, SchematicaUtil.getWorld(), pos, player());
		if (stack == null)
			return false;

		return stack.isItemEqual(player().getHeldItem());
	}

	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END || player() == null)
			return;

		if (SchematicaUtil.getWorld() == null) {
			schematic = null;
			requiredItems.clear();
			return;
		}

		if (SchematicaUtil.getSchematic() != schematic) {
			schematic = SchematicaUtil.getSchematic();
			updateItemList();
		}

		if (heldItem == null && player().getHeldItem() == null || player().getHeldItem() == heldItem)
			return;

		if (heldItem == null || player().getHeldItem() == null) {
			if (isHeldItemRequired(heldItem) || isHeldItemRequired(player().getHeldItem()))
				RenderSchematic.INSTANCE.refresh();

			heldItem = player().getHeldItem();
			return;
		}

		if (heldItem.isItemEqual(player().getHeldItem()) || (!isHeldItemRequired(heldItem) && !isHeldItemRequired(player().getHeldItem()))) {
			heldItem = player().getHeldItem();
			return;
		}

		heldItem = player().getHeldItem();
		RenderSchematic.INSTANCE.refresh();
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

					ItemStack stack = block.getPickBlock(mc().objectMouseOver, SchematicaUtil.getWorld(), pos, player());

					if (stack == null)
						continue;

					BlockPos worldPos = SchematicaUtil.getPosition().add(pos);
					Block worldBlock = world().getBlockState(worldPos).getBlock();
					ItemStack worldStack = worldBlock.getPickBlock(mc().objectMouseOver, world(), worldPos, player());
					if (stack.isItemEqual(worldStack)) // Required block is already placed
						continue;

					requiredItems.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
				}
			}
		}
	}

}
