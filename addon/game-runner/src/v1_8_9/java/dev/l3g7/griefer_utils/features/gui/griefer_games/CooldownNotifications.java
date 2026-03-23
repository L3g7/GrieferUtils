/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.griefer_games;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;

@Singleton
public class CooldownNotifications extends Feature {

	private static final String TITLE = "§8§m------------§r§8[ §r§6Cooldowns §r§8]§r§8§m------------§r";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private final List<String> validCooldowns = new ArrayList<>();
	private final Map<String, Long> endDates = Collections.synchronizedMap(new HashMap<>());
	private boolean cooldownsDisplayed = false;

	private CompletableFuture<Void> chatLock = null;
	private boolean waitingForCooldownsGui = false;
	private Integer cooldownsGuiId = null;
	private int cooldownsGuiSlotCount = 0;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Cooldown-Benachrichtigungen")
		.description("Zeigt die momentanen Cooldowns beim ersten Beitritt eines Citybuilds an.")
		.icon("bell")
		.callback(v -> {
			// If no data is found, open and close /cooldowns automatically
			if (v && endDates.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForCooldownsGui) {
				chatLock = ChatQueue.sendBlocking("/cooldowns", () -> {
					LabyBridge.labyBridge.notifyError("Cooldowns konnten nicht geöffnet werden!");
					waitingForCooldownsGui = false;
				});
				waitingForCooldownsGui = true;
			}
		});

	@EventListener
	private void onStaticData(StaticDataReceiveEvent event) {
		validCooldowns.addAll(Arrays.asList(event.data.cooldowns));
		for (String key : new ArrayList<>(endDates.keySet()))
			if (!validCooldowns.contains(key))
				endDates.remove(key);
	}

	/**
	 * Parses cooldowns from chat messages.
	 */
	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (event.message.getUnformattedText().matches("^Du hast .+-Booster erhalten\\. Danke für deine Unterstützung von GrieferGames!$"))
			endDates.put("/grieferboost", System.currentTimeMillis() + HOURS.toMillis(24 * 14) + 1000);
		else if (event.message.getUnformattedText().equals("[CaseOpening] Du hast 2 Kisten erhalten."))
			endDates.put("/freekiste", System.currentTimeMillis() + DAYS.toMillis(14) + 1000);
		else if (event.message.getUnformattedText().matches("^\\[Kopf] Du hast einen .+[ -]Kopf erhalten[!.]$"))
			endDates.put("/kopf", System.currentTimeMillis() + DAYS.toMillis(PlayerUtil.getRank(PlayerUtil.getName()).equals("Titan") ? 14 : 7));
		else if (event.message.getUnformattedText().matches("^Du hast .+ den Premium Rang aktiviert\\.$"))
			endDates.put("/premium", System.currentTimeMillis() + DAYS.toMillis(7));
		else if (event.message.getUnformattedText().equals("[StartKick] Ersteller: " + PlayerUtil.getName()))
			endDates.put("/startkick", System.currentTimeMillis() + HOURS.toMillis(12));
		else
			return;

		saveCooldowns();
	}

	/**
	 * Parses cooldowns from the GUI.
	 */
	@EventListener(triggerWhenDisabled = true)
	public void onGuiOpen(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		if (event.packet.getWindowTitle().getFormattedText().equals("§6Cooldowns§r")) {
			cooldownsGuiId = event.packet.getWindowId();
			cooldownsGuiSlotCount = event.packet.getSlotCount();
			if (waitingForCooldownsGui) {
				waitingForCooldownsGui = false;
				event.cancel();
			}
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiSetSlot(PacketReceiveEvent<S2FPacketSetSlot> event) {
		if (cooldownsGuiId != null && event.packet.func_149175_c() == cooldownsGuiId)
			parseAvailability(event.packet.func_149173_d(), event.packet.func_149174_e());
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiSetSlots(PacketReceiveEvent<S30PacketWindowItems> event) {
		if (cooldownsGuiId != null && event.packet.func_148911_c() == cooldownsGuiId) {
			int index = 0;
			for (ItemStack itemStack : event.packet.getItemStacks())
				parseAvailability(index++, itemStack);
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void cbSwitch(ServerSwitchEvent event) {
		cooldownsGuiId = null;
	}

	private void parseAvailability(int slot, ItemStack s) {
		if (slot > cooldownsGuiSlotCount)
			return;

		if (s == null || s.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
			return;

		// Load cooldown time from item
		String name = DrawUtils.removeColor(s.getDisplayName()).replace("-Befehl", "");
		if (!validCooldowns.isEmpty() && !validCooldowns.contains(name))
			return;

		endDates.put(name, getAvailability(s));
		if (chatLock != null)
			chatLock.complete(null);
		saveCooldowns();
	}

	/**
	 * Automatically opens the GUI in the background if no data is found
	 */
	@EventListener
	public void onCBJoin(CitybuildJoinEvent event) {
		if (!endDates.isEmpty())
			return;

		waitingForCooldownsGui = true;
		chatLock = ChatQueue.sendBlocking("/cooldowns", () -> waitingForCooldownsGui = false);
	}

	/**
	 * Announces the current cooldowns when joining the server for the first time.
	 */
	@EventListener
	public void onCitybuildJoin(CitybuildJoinEvent event) {
		if (cooldownsDisplayed)
			return;

		cooldownsDisplayed = true;

		if (endDates.size() == 0)
			// Cooldowns haven't been loaded yet
			return;

		synchronized (endDates) {
			endDates.keySet().forEach(this::checkEndTime);

			// Display cooldown information on server join
			display(TITLE);

			for (Map.Entry<String, Long> entry : endDates.entrySet())
				if (entry.getValue() == 0)
					display("§8» §e%s§7:§r %s", entry.getKey(), "§aVerfügbar");
			for (Map.Entry<String, Long> entry : endDates.entrySet())
				if (entry.getValue() > 0)
					display("§8» §e%s§7:§r %s", entry.getKey(), "§6Verfügbar am " + DATE_FORMAT.format(new Date(entry.getValue())));
			for (Map.Entry<String, Long> entry : endDates.entrySet())
				if (entry.getValue() < 0)
					display("§8» §e%s§7:§r %s", entry.getKey(), "§cNicht freigeschaltet");
		}

		display(TITLE);
	}

	/**
	 * Announces if a cooldown becomes available.
	 */
	@EventListener
	public void onTick(ClientTickEvent event) {
		// Check if cooldown has become available
		synchronized (endDates) {
			for (String command : endDates.keySet()) {
				if (checkEndTime(command)) {
					if (player() != null && world() != null)
						display(Constants.ADDON_PREFIX + "§e%s ist nun §averfügbar§e!", command);
					else
						LabyBridge.labyBridge.notify("Cooldown-Benachrichtigungen", String.format("%s ist nun §averfügbar§e!", command));

					saveCooldowns();
				}
			}
		}
	}

	private boolean checkEndTime(String name) {
		Long endTime = endDates.get(name);
		if (endTime > 0 && endTime < System.currentTimeMillis()) {
			endDates.put(name, 0L);
			return true;
		}
		return false;
	}

	private void saveCooldowns() {
		JsonObject o = new JsonObject();
		synchronized (endDates) {
			for (Map.Entry<String, Long> entry : endDates.entrySet())
				o.addProperty(entry.getKey(), entry.getValue());
		}

		// Save end dates along with player uuid so no problems occur when using multiple accounts
		Config.set("player.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId(), o);
		Config.save();
	}

	@EventListener
	public void loadCooldowns(GrieferGamesJoinEvent event) {
		String path = "player.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId();

		if (!Config.has(path))
			return;

		endDates.clear();
		for (Map.Entry<String, JsonElement> e : Config.get(path).getAsJsonObject().entrySet()) {
			if (e.getKey().startsWith("/clan") || e.getKey().contains("Riesige GS") || e.getKey().equals("/premium"))
				continue;

			if (validCooldowns.isEmpty() || validCooldowns.contains(e.getKey()))
				endDates.put(e.getKey(), e.getValue().getAsLong());
		}
	}

	/**
	 * -2: Invalid item
	 * -1: not available
	 * 0: available
	 * >0: unix time when available
	 */
	private static long getAvailability(ItemStack i) {
		List<String> lore = ItemUtil.getLore(i);
		if (lore.size() == 1) {
			if (lore.get(0).equals("§aVerfügbar"))
				return 0;

			return -1;
		} else if (lore.size() == 2) {
			String dateStr = lore.get(1)
				.replace("§7am §e§e", "")
				.replace(" §7um§e ", " ")
				.replace(" §7frei.", "");
			try {
				return DATE_FORMAT.parse(dateStr).getTime();
			} catch (ParseException e) {
				return -1;
			}
		}
		return -2;
	}

}
