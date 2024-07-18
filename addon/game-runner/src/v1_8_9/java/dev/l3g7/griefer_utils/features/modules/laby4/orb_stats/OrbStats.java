/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4.orb_stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.misc.ServerCheck.isOnGrieferGames;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_4)
public class OrbStats extends Laby4Module {

	private static final Pattern ORB_SELL_PATTERN = Pattern.compile("^\\[Orbs] Du hast erfolgreich (?<amount>[\\d.]+) (?<item>[\\S ]+) für (?<orbs>[\\d.]+) Orbs verkauft\\.$");
	private static final Pattern RANKING_PATTERN = Pattern.compile("§7(?<item>.*): §e(?<amount>[0-9]+).*");
	private static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMAN));
	private static final Map<String, String> GUI_TO_CHAT_MAPPING = new HashMap<>() {{
		put("Hasenfelle", "Hasenfell");
		put("Rohe Fische", "Roher Fisch");
		put("Roche Lachse", "Roher Lachs");
		put("Obsidianblöcke", "Obsidian");
		put("Prismarinblöcke", "Prismarinblock");
		put("Rosensträucher", "Rosenstrauch");
		put("Glowstoneblöcke", "Glowstoneblock");
		put("Seelensandblöcke", "Seelensand");
		put("Quarzerze", "Netherquarzerz");
		put("Quarze", "Netherquarz");
		put("Sandblöcke", "Sand");
		put("Rote Sandblöcke", "Roter Sand");
		put("Kiesblöcke", "Kies");
		put("Erdblöcke", "Erde");
		put("Myzelblöcke", "Myzel");
		put("Podsolblöcke", "Podsol");
		put("Grobe Erdblöcke", "grobe Erde");
		put("Farne", "Farn");
		put("Löwenzähne", "Löwenzahn");
		put("Mohne", "Mohn");
		put("Sternlauche", "Sternlauch");

		put("Smaragde", "Smaragden");

		put("Dioritblöcke", "Diorit");
		put("Granitblöcke", "Granit");
		put("Andesitblöcke", "Andesit");
		put("Polierte Dioritblöcke", "Polierter Diorit");
		put("Polierte Granitblöcke", "Polierter Granit");
		put("Polierte Andesitblöcke", "Polierter Andesit");
	}};

	private HashMap<Integer, Integer> stats = new HashMap<>();
	private String lastItem = null;
	private boolean waitingForGUI = false;
	private GuiScreen statsRevertScreen = null;
	private GuiChest lastScreen = null;
	private CompletableFuture<Void> guiInitBlock = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orb-Statistik")
		.description("Zeigt dir an, wie oft das zuletzt abgegebene Item insgesamt abgegeben wurde.")
		.icon("blue_graph")
		.callback(v -> {
			// If no data is found, open and close /stats automatically
			if (v && stats.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForGUI) {
				guiInitBlock = ChatQueue.sendBlocking("/stats", () -> {
					LabyBridge.labyBridge.notifyError("Stats konnten nicht geöffnet werden!");
					resetWaitingForGUI();
				});
				waitingForGUI = true;
				statsRevertScreen = mc().currentScreen;
			}
		});

	@Override
	public String getValue() {
		return lastItem == null ? "?" : lastItem + ": " + DECIMAL_FORMAT_3.format(stats.get(lastItem.hashCode()));
	}

	private void resetWaitingForGUI() {
		guiInitBlock.complete(null);
		mc().displayGuiScreen(statsRevertScreen);
		statsRevertScreen = null;
		waitingForGUI = false;
	}

	private void saveConfig() {
		String path = "modules.orb_stats.stats." + mc().getSession().getProfile().getId();

		if (lastItem != null)
			Config.set(path + ".last", new JsonPrimitive(lastItem));
		Config.set(path + ".data", new JsonPrimitive(HashMapSerializer.toString(stats)));
		Config.save();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		lastScreen = event.gui;
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadConfig(GrieferGamesJoinEvent ignored) {
		lastItem = null;
		stats.clear();

		String path = "modules.orb_stats.stats." + mc().getSession().getProfile().getId();

		if (Config.has(path + ".last"))
			lastItem = Config.get(path + ".last").getAsString();
		if (Config.has(path + ".data"))
			stats = HashMapSerializer.fromString(Config.get(path + ".data").getAsString());
	}

	@EventListener
	public void onCBJoin(CitybuildJoinEvent event) {
		if (!isOnGrieferGames())
			return;

		// If no data is found, open and close /stats automatically
		if (stats.isEmpty()) {
			guiInitBlock = ChatQueue.sendBlocking("/stats", () -> {
				LabyBridge.labyBridge.notifyError("Stats konnten nicht geöffnet werden!");
				resetWaitingForGUI();
			});
			waitingForGUI = true;
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMsgReceive(MessageReceiveEvent event) {
		Matcher matcher = ORB_SELL_PATTERN.matcher(event.message.getUnformattedText());
		if (!matcher.matches())
			return;

		lastItem = matcher.group("item");

		Slot s = lastScreen.inventorySlots.getSlot(11);
		// Both grass items and grass blocks have "Gras" as their name when selling them
		if (s.getHasStack() && lastItem.equals("Gras"))
			lastItem = s.getStack().getItem() == Item.getItemFromBlock(Blocks.grass) ? "Grasblöcke" : "Gräser";


		// Add the received orbs
		int addend = Integer.parseInt(matcher.group("amount").replace(".", ""));
		stats.compute(lastItem.hashCode(), (key, value) -> (value == null ? 0 : value) + addend);
		saveConfig();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!ServerCheck.isOnCitybuild() || !(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc().currentScreen, "lowerChestInventory");

		// When the players name contains a word that was blacklisted at some point, it is not included in the title
		if (!inv.getName().equals("§6Statistik von §e" + PlayerUtil.getName()) && !inv.getName().equals("§6Statistik"))
			return;

		// Check if it's the users stats that are open
		ItemStack skull = inv.getStackInSlot(10);
		String uuid = getUUIDFromSkullTexture(skull);

		if (!mc().getSession().getProfile().getId().toString().equalsIgnoreCase(uuid))
			return;

		// Inv hasn't been loaded yet
		if (inv.getStackInSlot(42) == null || inv.getStackInSlot(42).getItem() != Items.wheat)
			return;

		for (int i = 0; i < inv.getSizeInventory(); i++)
			extractInfo(inv.getStackInSlot(i));

		if (waitingForGUI)
			resetWaitingForGUI();

		saveConfig();
	}

	private void extractInfo(ItemStack stack) {
		if (stack == null || !stack.hasTagCompound())
			return;

		NBTTagCompound tag = stack.getTagCompound();
		NBTTagList lore = tag.getCompoundTag("display").getTagList("Lore", 8);

		for (int i = 0; i < lore.tagCount(); i++) {
			String line = lore.getStringTagAt(i);
			if (!line.contains("§7- §e"))
				continue;

			Matcher matcher = RANKING_PATTERN.matcher(line);
			if (!matcher.find())
				continue;


			int amount = Integer.parseInt(matcher.group("amount"));
			if (amount == 0)
				continue;

			String item = matcher.group("item");
			// Both clay and stained hardened clay are called "Tonblöcke" in the /stats gui
			if (item.equals("Tonblöcke") && stack.getItem() == Item.getItemFromBlock(Blocks.sand))
				item = "Ton";

			item = GUI_TO_CHAT_MAPPING.getOrDefault(item, item);

			// If no item was last used, it is set to the one with the highest amount
			if (lastItem == null || stats.get(lastItem.hashCode()) < amount)
				lastItem = item;

			stats.put(item.hashCode(), amount);
		}
	}

	private String getUUIDFromSkullTexture(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() != Items.skull || !itemStack.hasTagCompound())
			return null;

		NBTTagList textures = itemStack.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);
		if (textures.hasNoTags())
			return null;

		String b64 = ((NBTTagCompound) textures.get(0)).getString("Value");
		if (b64.isEmpty())
			return null;

		JsonObject object = Streams.parse(new JsonReader(new StringReader(new String(Base64.getDecoder().decode(b64))))).getAsJsonObject();
		if (!object.has("profileId"))
			return null;

		String uuidString = object.get("profileId").getAsString();
		return uuidString.replaceFirst("^(.{8})(.{4})(.{4})(.{4})(.{12})$", "$1-$2-$3-$4-$5");
	}

	private static class HashMapSerializer {
		public static String toString(HashMap<Integer, Integer> map) {
			ByteBuffer buf = ByteBuffer.allocate(map.size() * 8);

			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				buf.putInt(entry.getKey());
				buf.putInt(entry.getValue());
			}

			return Base64.getEncoder().encodeToString(buf.array());
		}

		public static HashMap<Integer, Integer> fromString(String b64) {
			HashMap<Integer, Integer> map = new HashMap<>();
			ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(b64));

			while (buf.hasRemaining())
				map.put(buf.getInt(), buf.getInt());

			return map;
		}

	}

}
