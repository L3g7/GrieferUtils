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

package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.ChatQueue;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
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
	public static ArrayList<SettingsElement> path() { return Reflection.get(mc().currentScreen, "path"); }
	public static WorldClient     world()           { return mc().theWorld; }

	public static ItemStack[]     armorInventory()  { return inventory().armorInventory; }
	public static InventoryPlayer inventory()       { return player().inventory; }

	public static int             screenWidth()     { return new ScaledResolution(mc()).getScaledWidth(); }
	public static int             screenHeight()    { return new ScaledResolution(mc()).getScaledHeight(); }
	public static float           partialTicks()    { return labyMod().getPartialTicks(); }

	public static LabyMod         labyMod()         { return LabyMod.getInstance(); }
	public static DrawUtils       drawUtils()       { return labyMod().getDrawUtils(); }

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

	public static void display(String message) {
		labyMod().displayMessageInChat(message);
	}
	public static void display(String format, Object... args) {
		display(String.format(format, args));
	}

	public static void displayAchievement(String title, String description) {
		displayAchievement("https://i.imgur.com/tWfT5Y8.png", title, description);
	}

	public static void displayAchievement (String iconUrl, String title, String description) {
		labyMod().getGuiCustomAchievement().displayAchievement(iconUrl, title, description);
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

	public static String getCityBuildAbbreviation(String citybuild) {
		if (citybuild.startsWith("CB"))
			return citybuild.substring(2);

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

}
