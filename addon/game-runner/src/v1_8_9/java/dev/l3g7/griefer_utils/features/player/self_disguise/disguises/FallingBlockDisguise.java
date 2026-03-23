package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * Disguised as {@link EntityFallingBlock}.
 */
public class FallingBlockDisguise extends Disguise<EntityFallingBlock> {

	private static final Map<String, Block> materials = new HashMap<>() {{
		put("dark_oak_door", Blocks.dark_oak_door);
		put("acacia_door", Blocks.acacia_door);
		put("jungle_door", Blocks.jungle_door);
		put("birch_door", Blocks.birch_door);
		put("spruce_door", Blocks.spruce_door);
		put("acacia_fence", Blocks.acacia_fence);
		put("dark_oak_fence", Blocks.dark_oak_fence);
		put("jungle_fence", Blocks.jungle_fence);
		put("birch_fence", Blocks.birch_fence);
		put("spruce_fence", Blocks.spruce_fence);
		put("acacia_fence_gate", Blocks.acacia_fence_gate);
		put("dark_oak_fence_gate", Blocks.dark_oak_fence_gate);
		put("jungle_fence_gate", Blocks.jungle_fence_gate);
		put("birch_fence_gate", Blocks.birch_fence_gate);
		put("spruce_fence_gate", Blocks.spruce_fence_gate);
		put("stone_slab2", Blocks.stone_slab2);
		put("double_stone_slab2", Blocks.double_stone_slab2);
		put("red_sandstone_stairs", Blocks.red_sandstone_stairs);
		put("red_sandstone", Blocks.red_sandstone);
		put("daylight_detector_inverted", Blocks.daylight_detector_inverted);
		put("packed_ice", Blocks.packed_ice);
		put("coal_block", Blocks.coal_block);
		put("hard_clay", Blocks.hardened_clay);
		put("carpet", Blocks.carpet);
		put("hay_block", Blocks.hay_block);
		put("sea_lantern", Blocks.sea_lantern);
		put("prismarine", Blocks.prismarine);
		put("iron_trapdoor", Blocks.iron_trapdoor);
		put("slime_block", Blocks.slime_block);
		put("dark_oak_stairs", Blocks.dark_oak_stairs);
		put("acacia_stairs", Blocks.acacia_stairs);
		put("stained_glass_pane", Blocks.stained_glass_pane);
		put("stained_clay", Blocks.stained_hardened_clay);
		put("dropper", Blocks.dropper);
		put("quartz_stairs", Blocks.quartz_stairs);
		put("quartz_block", Blocks.quartz_block);
		put("hopper", Blocks.hopper);
		put("quartz_ore", Blocks.quartz_ore);
		put("redstone_block", Blocks.redstone_block);
		put("daylight_detector", Blocks.daylight_detector);
		put("trapped_chest", Blocks.trapped_chest);
		put("anvil", Blocks.anvil);
		put("wood_button", Blocks.wooden_button);
		put("flower_pot", Blocks.flower_pot);
		put("cobble_wall", Blocks.cobblestone_wall);
		put("beacon", Blocks.beacon);
		put("jungle_wood_stairs", Blocks.jungle_stairs);
		put("birch_wood_stairs", Blocks.birch_stairs);
		put("spruce_wood_stairs", Blocks.spruce_stairs);
		put("emerald_block", Blocks.emerald_block);
		put("ender_chest", Blocks.ender_chest);
		put("emerald_ore", Blocks.emerald_ore);
		put("sandstone_stairs", Blocks.sandstone_stairs);
		put("wood_step", Blocks.wooden_slab);
		put("wood_double_step", Blocks.double_wooden_slab);
		put("ender_stone", Blocks.end_stone);
		put("brewing_stand", Blocks.brewing_stand);
		put("enchantment_table", Blocks.enchanting_table);
		put("nether_brick_stairs", Blocks.nether_brick_stairs);
		put("nether_fence", Blocks.nether_brick_fence);
		put("nether_brick", Blocks.nether_brick);
		put("water_lily", Blocks.waterlily);
		put("mycel", Blocks.mycelium);
		put("smooth_stairs", Blocks.stone_brick_stairs);
		put("brick_stairs", Blocks.brick_stairs);
		put("fence_gate", Blocks.oak_fence);
		put("vine", Blocks.vine);
		put("pumpkin_stem", Blocks.pumpkin_stem);
		put("melon_block", Blocks.melon_block);
		put("thin_glass", Blocks.glass_pane);
		put("iron_fence", Blocks.iron_bars);
		put("smooth_brick", Blocks.stonebrick);
		put("trap_door", Blocks.trapdoor);
		put("stained_glass", Blocks.stained_glass);
		put("glowstone", Blocks.glowstone);
		put("soul_sand", Blocks.soul_sand);
		put("netherrack", Blocks.netherrack);
		put("pumpkin", Blocks.pumpkin);
		put("fence", Blocks.oak_fence);
		put("jukebox", Blocks.jukebox);
		put("sugar_cane_block", Blocks.reeds);
		put("clay", Blocks.clay);
		put("cactus", Blocks.cactus);
		put("snow_block", Blocks.snow);
		put("ice", Blocks.ice);
		put("stone_button", Blocks.stone_button);
		put("redstone_ore", Blocks.redstone_ore);
		put("wood_plate", Blocks.wooden_pressure_plate);
		put("iron_door_block", Blocks.iron_door);
		put("stone_plate", Blocks.stone_pressure_plate);
		put("cobblestone_stairs", Blocks.stone_stairs);
		put("wooden_door", Blocks.oak_door);
		put("furnace", Blocks.furnace);
		put("soil", Blocks.farmland);
		put("workbench", Blocks.crafting_table);
		put("diamond_block", Blocks.diamond_block);
		put("diamond_ore", Blocks.diamond_ore);
		put("wood_stairs", Blocks.oak_stairs);
		put("mob_spawner", Blocks.mob_spawner);
		put("fire", Blocks.fire);
		put("obsidian", Blocks.obsidian);
		put("mossy_cobblestone", Blocks.mossy_cobblestone);
		put("bookshelf", Blocks.bookshelf);
		put("tnt", Blocks.tnt);
		put("brick", Blocks.brick_block);
		put("double_step", Blocks.double_stone_slab);
		put("iron_block", Blocks.iron_block);
		put("gold_block", Blocks.gold_block);
		put("red_mushroom", Blocks.red_mushroom);
		put("brown_mushroom", Blocks.brown_mushroom);
		put("red_rose", Blocks.red_flower);
		put("yellow_flower", Blocks.yellow_flower);
		put("wool", Blocks.wool);
		put("long_grass", Blocks.deadbush);
		put("web", Blocks.web);
		put("bed_block", Blocks.bed);
		put("note_block", Blocks.noteblock);
		put("sandstone", Blocks.sandstone);
		put("dispenser", Blocks.dispenser);
		put("lapis_block", Blocks.lapis_block);
		put("lapis_ore", Blocks.lapis_ore);
		put("glass", Blocks.glass);
		put("sponge", Blocks.sponge);
		put("leaves", Blocks.leaves);
		put("log", Blocks.log);
		put("coal_ore", Blocks.coal_ore);
		put("iron_ore", Blocks.iron_ore);
		put("gold_ore", Blocks.gold_ore);
		put("gravel", Blocks.gravel);
		put("sand", Blocks.sand);
		put("bedrock", Blocks.bedrock);
		put("sapling", Blocks.sapling);
		put("wood", Blocks.planks);
		put("cobblestone", Blocks.cobblestone);
		put("dirt", Blocks.dirt);
		put("grass", Blocks.grass);
		put("stone", Blocks.stone);
	}};

	public FallingBlockDisguise() {
		super(EntityFallingBlock.class);
	}

	@Override
	public boolean clampCoordinates(Arguments arguments) {
		return arguments.remove("block_coordinates");
	}

	@Override
	public EntityFallingBlock create(Arguments arguments) {
		Block material = Blocks.stone;
		int state = 0;

		for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); ) {
			String argument = iterator.next().toLowerCase();
			if (argument.startsWith("material=")) {
				String materialArg = argument.substring("material=".length());
				if (materials.containsKey(materialArg)) {
					iterator.remove();
					material = materials.get(materialArg);
				}
			}
		}

		for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); ) {
			String argument = iterator.next();
			if (argument.toLowerCase().startsWith("material_data=")) {
				state = Integer.parseInt(argument.substring("material_data=".length()));
				iterator.remove();
			}
		}

		return new EntityFallingBlock(world(), player().posX, player().posY, player().posZ, material.getStateFromMeta(state));
	}
}
