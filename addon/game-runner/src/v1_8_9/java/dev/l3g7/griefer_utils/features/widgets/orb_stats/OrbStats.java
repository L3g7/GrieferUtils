/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.orb_stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

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

import static dev.l3g7.griefer_utils.core.api.misc.Constants.ORB_SELL_PATTERN;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class OrbStats extends SimpleWidget {

	private static final Pattern RANKING_PATTERN = Pattern.compile("§7(?<item>.*): §e(?<amount>[0-9.]+).*");
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

	private HashMap<Integer, Long> stats = new HashMap<>();
	private String lastItem = null;
	private GuiChest lastScreen = null;

	private CompletableFuture<Void> chatLock = null;
	private boolean waitingForStatsGui = false;
	private boolean isOwnStatsGui = false;
	private Integer statsGuiId = null;
	private int statsGuiSlotCount = 0;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orb-Statistik")
		.description("Zeigt dir an, wie oft das zuletzt abgegebene Item insgesamt abgegeben wurde.")
		.icon("orb")
		.callback(v -> {
			// If no data is found, open and close /stats automatically
			if (v && stats.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForStatsGui) {
				chatLock = ChatQueue.sendBlocking("/stats", () -> {
					LabyBridge.labyBridge.notifyError("Stats konnten nicht geöffnet werden!");
					waitingForStatsGui = false;
				});
				waitingForStatsGui = true;
			}
		});

	/**
	 * Parses stats from selling items.
	 */
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
		saveStats();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		lastScreen = event.gui;
	}

	/**
	 * Parses stats from the GUI.
	 */
	@EventListener(triggerWhenDisabled = true)
	public void onGuiOpen(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		String invName = event.packet.getWindowTitle().getFormattedText();
		if (!(invName.equals("§6Statistik§r")
			|| invName.equals("§6Statistik von §e" + PlayerUtil.getName())
			|| invName.equals("§6Statistik von §e" + PlayerUtil.getName() + "§r")))
			return;

		statsGuiId = event.packet.getWindowId();
		statsGuiSlotCount = event.packet.getSlotCount();
		isOwnStatsGui = false;
		if (waitingForStatsGui) {
			waitingForStatsGui = false;
			event.cancel();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiSetSlot(PacketReceiveEvent<S2FPacketSetSlot> event) {
		if (statsGuiId != null && event.packet.func_149175_c() == statsGuiId)
			extractInfo(event.packet.func_149173_d(), event.packet.func_149174_e());
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiSetSlots(PacketReceiveEvent<S30PacketWindowItems> event) {
		if (statsGuiId != null && event.packet.func_148911_c() == statsGuiId) {
			int index = 0;
			for (ItemStack itemStack : event.packet.getItemStacks())
				extractInfo(index++, itemStack);
		}
	}

	/**
	 * Automatically opens the GUI in the background if no data is found
	 */
	@EventListener
	public void onCBJoin(CitybuildJoinEvent event) {
		if (!stats.isEmpty())
			return;

		waitingForStatsGui = true;
		chatLock = ChatQueue.sendBlocking("/stats", () -> waitingForStatsGui = false);
	}

	@Override
	public String getValue() {
		return lastItem == null || stats.isEmpty() ? "?" : lastItem + ": " + DECIMAL_FORMAT_3.format(stats.get(lastItem.hashCode()));
	}

	private void saveStats() {
		String path = "modules.orb_stats.stats." + mc().getSession().getProfile().getId();

		if (lastItem != null)
			Config.set(path + ".last", new JsonPrimitive(lastItem));
		Config.set(path + ".data", new JsonPrimitive(HashMapSerializer.toString(stats)));
		Config.save();
	}

	@EventListener(triggerWhenDisabled = true)
	public void loadStats(GrieferGamesJoinEvent ignored) {
		lastItem = null;
		stats.clear();

		String path = "modules.orb_stats.stats." + mc().getSession().getProfile().getId();

		if (Config.has(path + ".last"))
			lastItem = Config.get(path + ".last").getAsString();
		if (Config.has(path + ".data"))
			stats = HashMapSerializer.fromString(Config.get(path + ".data").getAsString());
	}

	private void extractInfo(int slot, ItemStack stack) {
		if (slot > statsGuiSlotCount)
			return;

		if (stack == null || !stack.hasTagCompound())
			return;

		// Check if the stats are yours
		if (slot == 10)
			isOwnStatsGui = mc().getSession().getProfile().getId().toString().equalsIgnoreCase(getUUIDFromSkullTexture(stack));

		if (!isOwnStatsGui)
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

			long amount = Long.parseLong(matcher.group("amount").replace(".", ""));
			if (amount == 0)
				continue;

			String item = matcher.group("item");
			// Both clay and stained hardened clay are called "Tonblöcke" in the /stats gui
			if (item.equals("Tonblöcke") && stack.getItem() == Item.getItemFromBlock(Blocks.sand))
				item = "Ton";

			item = GUI_TO_CHAT_MAPPING.getOrDefault(item, item);

			// If no item was last used, it is set to the one with the highest amount
			if (lastItem == null || !stats.containsKey(lastItem.hashCode()) || stats.get(lastItem.hashCode()) < amount)
				lastItem = item;

			stats.put(item.hashCode(), amount);
			if (chatLock != null)
				chatLock.complete(null);
			saveStats();
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

		JsonObject object;
		try {
			object = IO.read(Base64.getDecoder().decode(b64)).asJsonObject();
		} catch (Throwable t) {
			return null; // Invalid Base64
		}
		if (!object.has("profileId"))
			return null;

		String uuidString = object.get("profileId").getAsString();
		return uuidString.replaceFirst("^(.{8})(.{4})(.{4})(.{4})(.{12})$", "$1-$2-$3-$4-$5");
	}

	private static class HashMapSerializer {

		public static String toString(HashMap<Integer, Long> map) {
			ByteBuffer buf = ByteBuffer.allocate(map.size() * 12);

			for (Map.Entry<Integer, Long> entry : map.entrySet()) {
				buf.putInt(entry.getKey());
				buf.putLong(entry.getValue());
			}

			return Base64.getEncoder().encodeToString(buf.array());
		}

		public static HashMap<Integer, Long> fromString(String b64) {
			HashMap<Integer, Long> map = new HashMap<>();
			ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(b64));

			try {
				while (buf.hasRemaining())
					map.put(buf.getInt(), buf.getLong());
			} catch (Throwable t) {
				return new HashMap<>();
			}

			return map;
		}

	}

}
