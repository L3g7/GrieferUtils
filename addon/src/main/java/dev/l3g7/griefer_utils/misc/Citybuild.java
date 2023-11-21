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

package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.misc.gui.elements.DropDown;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;

public enum Citybuild implements DropDown.ItemEnum, Named {

	ANY(nether_star, "Egal","Egal"),

	CB1(diamond_block, 1),
	CB2(emerald_block, 2),
	CB3(gold_block, 3),
	CB4(redstone_block, 4),
	CB5(lapis_block, 5),
	CB6(coal_block, 6),
	CB7(emerald_ore, 7),
	CB8(redstone_ore, 8),
	CB9(diamond_ore, 9),
	CB10(gold_ore, 10),
	CB11(iron_ore, 11),
	CB12(coal_ore, 12),
	CB13(lapis_ore, 13),
	CB14(bedrock, 14),
	CB15(gravel, 15),
	CB16(obsidian, 16),
	CB17(stone, 6, 17),
	CB18(iron_block, 18),
	CB19(prismarine, 2, 19),
	CB20(prismarine, 20),
	CB21(mossy_cobblestone, 21),
	CB22(brick_block, 22),

	NATURE(sapling, 5, "nature", "Nature", "n"),
	EXTREME(sapling, 3, "extreme", "Extreme", "x"),
	CBE(netherrack, "cbevil", "Evil", "e", "cbe", "CB Evil"),

	WATER(water_bucket, "farm1", "Wasser", "w"),
	LAVA(lava_bucket, "nether1", "Lava", "l"),
	EVENT(beacon, "eventserver", "Event", "v");

	Citybuild(Block block, int citybuildId) {
		this(block, "cb" + citybuildId, "Citybuild " + citybuildId);
	}

	Citybuild(Block block, int meta, int citybuildId) {
		this(block, meta, "cb" + citybuildId, "Citybuild " + citybuildId);
	}

	Citybuild(Item item, String internalName, String displayName, String... aliases) {
		this(new ItemStack(item), internalName, displayName, aliases);
	}

	Citybuild(Block block, String internalName, String displayName, String... aliases) {
		this(block, 0, internalName, displayName, aliases);
	}

	Citybuild(Block block, int meta, String internalName, String displayName, String... aliases) {
		this(new ItemStack(block, 1, meta), internalName, displayName, aliases);
	}

	private final ItemStack stack;
	private final String internalName;
	private final String displayName;
	private final String[] aliases;

	Citybuild(ItemStack stack, String internalName, String displayName, String[] aliases) {
		this.stack = stack;
		this.internalName = internalName;
		this.displayName = displayName;
		this.aliases = aliases;
	}

	public static Citybuild getCitybuild(String cb) {
		cb = cb.toLowerCase();
		if (cb.startsWith("cb"))
			cb = cb.substring(2).trim();

		if (cb.startsWith("citybuild"))
			cb = cb.substring("citybuild".length()).trim();

		if (StringUtils.isNumeric(cb)) {
			try {
				return valueOf("CB" + cb);
			} catch (IllegalArgumentException ignored) {
				return Citybuild.ANY;
			}
		}

		for (Citybuild citybuild : values()) {
			if (citybuild.matches(cb))
				return citybuild;
		}

		return Citybuild.ANY;
	}

	public String getInternalName() {
		return this.internalName;
	}

	@Override
	public ItemStack getItem() {
		return stack;
	}

	public boolean isOnCb() {
		if (this == ANY)
			return true;

		return matches(MinecraftUtil.getServerFromScoreboard());
	}

	public void join() {
		if (internalName == null)
			throw new IllegalStateException("This citybuild does not exist");

		if (!ServerCheck.isOnGrieferGames()) {
			MinecraftUtil.display(Constants.ADDON_PREFIX + "§fBitte betrete GrieferGames.");
			return;
		}

		String cb = MinecraftUtil.getServerFromScoreboard();
		if (cb.equals("Portal") || cb.equals("Lobby")) {
			MinecraftUtil.display(Constants.ADDON_PREFIX + "§fBitte betrete einen Citybuild.");
			return;
		}

		MinecraftUtil.send("/switch " + internalName);
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean matches(String cb) {
		if (cb == null)
			return false;

		for (String alias : aliases)
			if (alias.equalsIgnoreCase(cb))
				return true;

		return cb.equalsIgnoreCase(displayName) || cb.equalsIgnoreCase(internalName) || name().equalsIgnoreCase(cb);
	}

	@Override
	public String getName() {
		return displayName;
	}

}
