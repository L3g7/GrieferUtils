/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.world.furniture;

import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.CustomItemBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import dev.l3g7.griefer_utils.features.world.furniture.blockshapes.BlockShapesLoader;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Registerer {
	private static final List<VersionBlock> blocks = new ArrayList<>();

	public static void registerBlocks() {
		for (VersionBlock block : blocks) {
			BlockShapesLoader.SHAPES_FUTURE.whenComplete((a, b) -> block.addShapes());
			ResourceLocation loc = new ResourceLocation("griefer_utils", block.getUnlocalizedName().substring(5));
			try {
				Reflection.invoke(GameData.getBlockRegistry(), "add", block.getModBlock().getBlockKey().getId(), loc, block);
			} catch (Throwable t) {
				ModBlocks.BlockKey key = block.getModBlock().getBlockKey();
				System.err.printf("Error with %s (ID: %d)%n", key.getLocation(), key.getId());
				t.printStackTrace();
			}
		}
	}

	public static void registerRenders() {
		Map<Block, Item> BLOCK_TO_ITEM = Reflection.get(Item.class, "BLOCK_TO_ITEM");
		ModBlocks.getBlocks().forEach((key, block) -> {
			if (!block.hasItem())
				return;

			Block mcBlock = block.getBlockHandle();
			Block suppliedBlock = block.getBlockToSupplyToItem().getBlockHandle();
			CustomItemBlock item = new CustomItemBlock(mcBlock, suppliedBlock);
			BLOCK_TO_ITEM.put(suppliedBlock, item);
			Item.itemRegistry.register(key.getId(), new ResourceLocation(key.getLocation()), item);

			ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			ModelResourceLocation modelResourceLocation = new ModelResourceLocation(key.getLocation(), "inventory");
			mesher.register(item, 0, modelResourceLocation);

		});
	}

	static  {
		ModBlocks.getBlocks().forEach((key, block) -> {
			VersionBlock mcBlock;
			Class<VersionBlock> versionBlockClass;
			VersionBlock.setRegisteringBlock(block);
			try {
				versionBlockClass = block.getVersionBlockClass() != null ? (Class<VersionBlock>) Class.forName("dev.l3g7.griefer_utils.features.world.furniture.block.version_specific." + block.getVersionBlockClass()) : VersionBlock.class;
			} catch (ClassNotFoundException e) {
				System.err.println("Error during registration of " + key.getLocation());
				throw new RuntimeException("VersionBlockClass for " + key.getLocation() + " does not exist!");
			}

			try {
				mcBlock = versionBlockClass.getConstructor(ModBlock.class).newInstance(block);
			} catch (Throwable t) {
				System.err.println(block.getBlockKey().getLocation() + "threw " + t.getClass().getSimpleName());
				t.printStackTrace();
				return;
			}

			if (block.getLightLevel() > 0.0f)
				mcBlock.setLightLevel(block.getLightLevel());

			mcBlock.setUnlocalizedName(key.getLocation().split(":")[1]);
			mcBlock.setCreativeTab(FurnitureTab.INSTANCE);
			block.setBlockHandle(mcBlock);
			blocks.add(mcBlock);
		});

	}

}
