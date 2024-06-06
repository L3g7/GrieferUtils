/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.item.ItemStack;

import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;

public enum Citybuild implements Named {

	ANY(new ItemStack(nether_star), "Egal", "Egal"),

	CB1(new ItemStack(diamond_block)),
	CB2(new ItemStack(emerald_block)),
	CB3(new ItemStack(gold_block)),
	CB4(new ItemStack(redstone_block)),
	CB5(new ItemStack(lapis_block)),
	CB6(new ItemStack(coal_block)),
	CB7(new ItemStack(emerald_ore)),
	CB8(new ItemStack(redstone_ore)),
	CB9(new ItemStack(diamond_ore)),
	CB10(new ItemStack(gold_ore)),
	CB11(new ItemStack(iron_ore)),
	CB12(new ItemStack(coal_ore)),
	CB13(new ItemStack(lapis_ore)),
	CB14(new ItemStack(bedrock)),
	CB15(new ItemStack(gravel)),
	CB16(new ItemStack(obsidian)),
	CB17(new ItemStack(stone, 1, 6)),
	CB18(new ItemStack(iron_block)),
	CB19(new ItemStack(prismarine, 1, 2)),
	CB20(new ItemStack(prismarine)),
	CB21(new ItemStack(mossy_cobblestone)),
	CB22(new ItemStack(brick_block)),

	NATURE(new ItemStack(sapling, 1, 5), "nature", "Nature", "n"),
	EXTREME(new ItemStack(sapling, 1, 3), "extreme", "Extreme", "x"),
	CBE(new ItemStack(netherrack), "cbevil", "Evil", "e", "cbe", "CB Evil"),

	WATER(new ItemStack(water_bucket), "farm1", "Wasser", "w"),
	LAVA(new ItemStack(lava_bucket), "nether1", "Lava", "l"),
	EVENT(new ItemStack(beacon), "eventserver", "Event", "v");

	private final String internalName;
	private final String displayName;
	private final String[] aliases;
	private final ItemStack stack;

	Citybuild(ItemStack stack) {
		String id = name().substring(2);
		internalName = "cb" + id;
		displayName = "Citybuild " + id;
		aliases = new String[0];
		this.stack = stack;
	}

	Citybuild(ItemStack stack, String internalName, String displayName, String... aliases) {
		this.internalName = internalName;
		this.displayName = displayName;
		this.aliases = aliases;
		this.stack = stack;
	}

	@Override
	public String getName() {
		return displayName;
	}

	public String getInternalName() {
		return internalName;
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
			LabyBridge.display(Constants.ADDON_PREFIX + "§fBitte betrete GrieferGames.");
			return;
		}

		String cb = MinecraftUtil.getServerFromScoreboard();
		if (cb.equals("Portal") || cb.equals("Lobby")) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§fBitte betrete einen Citybuild.");
			return;
		}

		ChatQueue.send("/switch " + internalName);
	}

	public boolean matches(String cb) {
		if (cb == null)
			return false;

		for (String alias : aliases)
			if (alias.equalsIgnoreCase(cb))
				return true;

		return cb.equalsIgnoreCase(displayName) || cb.equalsIgnoreCase(internalName) || name().equalsIgnoreCase(cb);
	}

	public static Citybuild getCitybuild(String cb) {
		cb = cb.toLowerCase();
		if (cb.startsWith("cb"))
			cb = cb.substring(2).trim();

		if (cb.startsWith("citybuild"))
			cb = cb.substring("citybuild".length()).trim();

		if (StringUtil.isNumeric(cb)) {
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

	public ItemStack toItemStack() {
		return stack;
	}

}
