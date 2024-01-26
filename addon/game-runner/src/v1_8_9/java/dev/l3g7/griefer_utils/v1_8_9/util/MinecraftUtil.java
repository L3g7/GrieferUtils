/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.util;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.v1_8_9.misc.ChatQueue;
import dev.l3g7.griefer_utils.v1_8_9.misc.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.UUID;

/**
 * A utility class for simplified access to parts of Minecraft (and LabyMod).
 */
public class MinecraftUtil {

	public static final int FONT_HEIGHT = 9;
	private static final int HOUR = 60 * 60 * 1000; // An hour, in milliseconds.

	public static Minecraft       mc()              { return Minecraft.getMinecraft(); }
	public static EntityPlayerSP  player()          { return mc().thePlayer; }
	public static String          name()            { return mc().getSession().getUsername(); }
	public static GameSettings    settings()        { return mc().gameSettings; }
	public static WorldClient     world()           { return mc().theWorld; }

	public static ItemStack[]     armorInventory()  { return inventory().armorInventory; }
	public static InventoryPlayer inventory()       { return player().inventory; }

	public static int             screenWidth()     { return new ScaledResolution(mc()).getScaledWidth(); }
	public static int             screenHeight()    { return new ScaledResolution(mc()).getScaledHeight(); }

	public static float           partialTicks()    { return LabyBridge.labyBridge.partialTicks(); }

	public static UUID uuid() {
		return mc().getSession().getProfile().getId();
	}

	public static AxisAlignedBB axisAlignedBB(Vec3d a, Vec3d b) {
		return new AxisAlignedBB(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Block blockAt(Vec3d pos) {
		return world().getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock();
	}

	public static Vec3d pos(Entity e) {
		return new Vec3d(e.posX, e.posY, e.posZ);
	}

	public static Vec3d renderPos() {
		RenderManager renderManager = mc().getRenderManager();
		double x = Reflection.get(renderManager, "renderPosX");
		double y = Reflection.get(renderManager, "renderPosY");
		double z = Reflection.get(renderManager, "renderPosZ");
		return new Vec3d(x, y, z);
	}

	public static void send(String message) {
		ChatQueue.send(message);
	}

	public static void send(String format, Object... args) {
		send(String.format(format, args));
	}

	public static void suggest(String message) {
		mc().displayGuiScreen(new GuiChat(message));
	}

	public static void suggest(String format, Object... args) {
		suggest(String.format(format, args));
	}

	public static String getServerFromScoreboard() {
		if (world() == null)
			return "";

		ScorePlayerTeam team = world().getScoreboard().getTeam("server_value");
		return team == null ? "" : team.getColorPrefix().replaceAll("§.", "");
	}

	public static Citybuild getCurrentCitybuild() {
		return Citybuild.getCitybuild(getServerFromScoreboard());
	}

	public static String getCitybuildAbbreviation(String citybuild) {
		if (citybuild.startsWith("CB"))
			return citybuild.substring(2);
		if (citybuild.startsWith("Citybuild "))
			return citybuild.substring(10);

		switch (citybuild) {
			case "Nature": return "N";
			case "Extreme": return "X";
			case "Evil": return "E";
			case "Wasser": return "W";
			case "Lava": return "L";
			case "Event": return "V";
			default: return "*";
		}
	}

	public static long getNextServerRestart() {
		long time = System.currentTimeMillis();
		long reset = time - time % (24 * HOUR) + (2 * HOUR); // Get timestamp for 02:00 UTC on the current day

		if (System.currentTimeMillis() > reset)
			reset += 24 * HOUR; // When it's already after 02:00 UTC, the next reset is 24h later

		return reset;
	}

	public static String getGuiChestTitle() {
		if (!(mc().currentScreen instanceof GuiChest))
			return "";

		IInventory lowerChestInventory = Reflection.get(mc().currentScreen, "lowerChestInventory");
		return lowerChestInventory.getDisplayName().getFormattedText();
	}

	public static Slot getSlotUnderMouse(GuiScreen guiScreen) {
		return Reflection.get(guiScreen, "theSlot");
	}

	public static ItemStack getStackUnderMouse(GuiScreen guiScreen) {
		Slot slot = getSlotUnderMouse(guiScreen);
		return slot == null ? null : slot.getStack();
	}

	public static int getSlotIndex(Slot slot) {
		return Reflection.get(slot, "slotIndex");
	}

	public static ItemStack getPickBlock(Block block, World world, BlockPos pos) {
		Item item = block.getItem(world, pos);
		if (item == null)
			return null;

		Block itemBlock = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
		return new ItemStack(item, 1, itemBlock.getDamageValue(world, pos));
	}

	public static int getButtonHeight(GuiButton button) {
		return Reflection.get(button, "height");
	}

	/**
	 * Guesses whether the player is in a farmwelt based upon the amount of bedrock on layer 3
	 */
	public static boolean isInFarmwelt() {
		int checkedBlocks = 0;
		int bedrocksFound = 0;

		for (int chunkDX = -3; chunkDX <= 3; chunkDX++) {
			for (int chunkDZ = -3; chunkDZ <= 3; chunkDZ++) {
				Chunk chunk = world().getChunkFromChunkCoords(player().chunkCoordX + chunkDX, player().chunkCoordZ + chunkDZ);
				if (!chunk.isLoaded() || chunk.isEmpty())
					continue;

				checkedBlocks += 256;
				int blocksFound = 0;
				for (int x = 0; x < 16; x++)
					for (int z = 0; z < 16; z++)
						if (chunk.getBlock(x, 3, z) == Blocks.bedrock)
							blocksFound++;

				if (blocksFound == 256) {
					// Player is at spawn
					return false;
				}

				bedrocksFound += blocksFound;
			}
		}

		return (bedrocksFound / (float) checkedBlocks) > 0.33f;
	}

	public static void drawString(String text, float x, float y, int lvt41, boolean lvt51) {
	}
}
