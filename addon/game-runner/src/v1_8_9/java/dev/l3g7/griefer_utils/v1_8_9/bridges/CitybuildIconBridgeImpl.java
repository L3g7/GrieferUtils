package dev.l3g7.griefer_utils.v1_8_9.bridges;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import net.minecraft.item.ItemStack;

import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;

@Bridge
@Singleton
public class CitybuildIconBridgeImpl implements Citybuild.CitybuildIconBridge {

	@Override
	@SuppressWarnings("DuplicatedCode") // intellij is brain-dead
	public ItemStack toItemStack(Citybuild cb) {
		return switch (cb) {
			case ANY -> new ItemStack(nether_star);
			case CB1 -> new ItemStack(diamond_block);
			case CB2 -> new ItemStack(emerald_block);
			case CB3 -> new ItemStack(gold_block);
			case CB4 -> new ItemStack(redstone_block);
			case CB5 -> new ItemStack(lapis_block);
			case CB6 -> new ItemStack(coal_block);
			case CB7 -> new ItemStack(emerald_ore);
			case CB8 -> new ItemStack(redstone_ore);
			case CB9 -> new ItemStack(diamond_ore);
			case CB10 -> new ItemStack(gold_ore);
			case CB11 -> new ItemStack(iron_ore);
			case CB12 -> new ItemStack(coal_ore);
			case CB13 -> new ItemStack(lapis_ore);
			case CB14 -> new ItemStack(bedrock);
			case CB15 -> new ItemStack(gravel);
			case CB16 -> new ItemStack(obsidian);
			case CB17 -> new ItemStack(stone, 1, 6);
			case CB18 -> new ItemStack(iron_block);
			case CB19 -> new ItemStack(prismarine, 1, 2);
			case CB20 -> new ItemStack(prismarine);
			case CB21 -> new ItemStack(mossy_cobblestone);
			case CB22 -> new ItemStack(brick_block);
			case NATURE -> new ItemStack(sapling, 1, 5);
			case EXTREME -> new ItemStack(sapling, 1, 3);
			case CBE -> new ItemStack(netherrack);
			case WATER -> new ItemStack(water_bucket);
			case LAVA -> new ItemStack(lava_bucket);
			case EVENT -> new ItemStack(beacon);
		};
	}

}
