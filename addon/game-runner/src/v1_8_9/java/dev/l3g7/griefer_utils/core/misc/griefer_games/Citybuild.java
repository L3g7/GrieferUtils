/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.griefer_games;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Option;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;

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
	EVENT(new ItemStack(beacon), "eventserver", "Event", "v"),
	MAGIC_FOREST(new ItemStack(Blocks.mycelium), "zauberwald", "Zauberwald", "z", "zw"),

	PORTAL(null, "portal", "Portal"),
	LOBBY(null, "lobby", "Lobby"),
	TEST(null, "cbt", "Test", "t"),
	UNKNOWN(null, "unknown", "Unbekannt");

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

	public String getAbbreviation() {
		String name = getName();
		if (name.startsWith("CB"))
			return name.substring(2);
		if (name.startsWith("Citybuild "))
			return name.substring(10);

		return switch (name) {
			case "Nature" -> "N";
			case "Extreme" -> "X";
			case "Evil" -> "E";
			case "Wasser" -> "W";
			case "Lava" -> "L";
			case "Event" -> "V";
			case "Test" -> "T";
			case "Egal" -> "*";
			default -> "?";
		};
	}

	public boolean isOnCb() {
		if (this == ANY)
			return true;

		return current() == this;
	}

	public boolean isValid() {
		return stack != null;
	}

	public boolean hasPlots() {
		return stack != null && this != LAVA && this != WATER && this != MAGIC_FOREST;
	}

	public void join() {
		if (this == UNKNOWN)
			throw new IllegalStateException("This citybuild does not exist");

		if (!ServerCheck.isOnGrieferGames()) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§fBitte betrete GrieferGames.");
			return;
		}

		if (this == Citybuild.PORTAL)
			ChatQueue.send("/portal");
		if (this == Citybuild.LOBBY)
			ChatQueue.send("/hub");
		else {
			// Normal citybuilds
			if (current() == Citybuild.PORTAL)
				ChatQueue.send("/hub");

			ChatQueue.send("/switch " + internalName);
		}
	}

	public boolean matches(String cb) {
		if (cb == null)
			return false;

		for (String alias : aliases)
			if (alias.equalsIgnoreCase(cb))
				return true;

		return cb.equalsIgnoreCase(displayName) || cb.equalsIgnoreCase(internalName) || name().equalsIgnoreCase(cb);
	}

	public ItemStack toItemStack() {
		return stack;
	}

	public static Citybuild parse(String cb) {
		return tryParse(cb).getOr(Citybuild.UNKNOWN);
	}

	public static Option<Citybuild> tryParse(String cb) {
		cb = cb.toLowerCase();
		if (cb.startsWith("cb"))
			cb = cb.substring(2).trim();

		if (cb.startsWith("citybuild"))
			cb = cb.substring("citybuild".length()).trim();

		if (StringUtil.isNumeric(cb)) {
			try {
				return Option.of(valueOf("CB" + cb));
			} catch (IllegalArgumentException ignored) {
				return Option.empty();
			}
		}

		for (Citybuild citybuild : values()) {
			if (citybuild.matches(cb))
				return Option.of(citybuild);
		}

		return Option.empty();
	}

	public static Citybuild current() {
		return Tracker.currentCitybuild;
	}

	private static class Tracker {

		private static Citybuild currentCitybuild = UNKNOWN;

		@EventListener(priority = Priority.HIGH)
		private static void onCitybuildInit(PacketReceiveEvent<S47PacketPlayerListHeaderFooter> event) {
			String[] lines = event.packet.getHeader().getFormattedText().split("\n");
			if (lines.length != 3) {
				currentCitybuild = UNKNOWN;
				return;
			}

			String cbLine = lines[2].replaceAll("§.", "");
			if (!cbLine.startsWith("Aktueller Server: ")) {
				currentCitybuild = UNKNOWN;
				return;
			}

			currentCitybuild = parse(cbLine.substring("Aktueller Server: ".length()));
		}
	}

}
