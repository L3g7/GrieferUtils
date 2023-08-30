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

package dev.l3g7.griefer_utils.features.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ChatQueue;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.main.LabyMod;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;

@Singleton
public class CooldownNotifications extends Feature {

	private static final String TITLE = "§8§m------------§r§8[ §r§6Cooldowns §r§8]§r§8§m------------§r";
	public final Map<String, Long> endDates = new HashMap<>();
	private boolean waitingForCooldownGUI = false;
	private boolean sendCooldowns = false;
	private CompletableFuture<Void> guiInitBlock = null;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Cooldown-Benachrichtigungen")
		.description("Zeigt die momentanen Cooldowns beim ersten Beitritt eines Citybuilds an.")
		.icon(Material.WATCH)
		.callback(v -> {
			// If no data is found, open and close /cooldowns automatically
			if (v && endDates.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForCooldownGUI) {
				guiInitBlock = ChatQueue.sendBlocking("/cooldowns", () -> {
					displayAchievement("§c§lFehler \u26A0", "§c/cooldowns geht nicht!");
					resetWaitingForGUI();
				});
				waitingForCooldownGUI = true;
			}
		});

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (event.message.getUnformattedText().matches("^Du hast .+-Booster erhalten\\. Danke für deine Unterstützung von GrieferGames!$"))
			endDates.put("/grieferboost", System.currentTimeMillis() + HOURS.toMillis(24 * 14 - 1) + 1000);
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

	@EventListener
	public void onCBJoin(CityBuildJoinEvent event) {
		// If no data is found, open and close /cooldowns automatically
		if (endDates.isEmpty()) {
			guiInitBlock = ChatQueue.sendBlocking("/cooldowns", () -> {
				displayAchievement("§c§lFehler \u26A0", "§c/cooldowns geht nicht!");
				resetWaitingForGUI();
			});
			waitingForCooldownGUI = true;
		}
	}

	@EventListener(priority = Priority.LOWEST) // Make sure loadCooldowns is triggered before
	public void onServerJoin(GrieferGamesJoinEvent event) {
		sendCooldowns = true;
	}

	@EventListener
	public void onCityBuildJoin(CityBuildJoinEvent event) {
		if (!sendCooldowns)
			return;

		sendCooldowns = false;

		// Cooldowns haven't been loaded yet
		if (endDates.size() == 0)
			return;

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

		display(TITLE);
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		// Display in chat if in game, if not display as achievement
		Consumer<String> displayFunc = LabyMod.getInstance().isInGame()
			? s -> display(Constants.ADDON_PREFIX + "§e%s ist nun §averfügbar§e!", s)
			: s -> displayAchievement(Constants.ADDON_NAME, String.format("§e%s ist nun §averfügbar§e!", s));

		// Check if cooldown has become available
		for (String command : endDates.keySet()) {
			if (checkEndTime(command)) {
				displayFunc.accept(command);
				saveCooldowns();
			}
		}
	}

	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		// Check if cooldown gui is open
		if (mc().currentScreen instanceof GuiChest) {
			IInventory inventory = Reflection.get(mc().currentScreen, "lowerChestInventory");
			if (inventory.getDisplayName().getFormattedText().equals("§6Cooldowns§r")) {
				if (inventory.getSizeInventory() != 45 || inventory.getStackInSlot(11) == null || inventory.getStackInSlot(11).getItem() != Items.gold_ingot)
					return;

				// Iterate through slots
				boolean foundAny = false;
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					ItemStack s = inventory.getStackInSlot(i);
					if (s == null || s.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
						continue;

					// Load cooldown time from item
					String name = ModColor.removeColor(s.getDisplayName()).replace("-Befehl", "");
					if (name.startsWith("/clan") || name.equals("Riesige GS überschreiben") || name.equals("/premium"))
						continue;

					endDates.put(name, getAvailability(s));
					foundAny = true;
				}

				if (foundAny) {
					saveCooldowns();
					// Close cooldowns if waitingForCooldownGUI (was automatically opened)
					if (waitingForCooldownGUI)
						resetWaitingForGUI();
				}
			}
		}
	}

	private void resetWaitingForGUI() {
		mc().displayGuiScreen(null);
		guiInitBlock.complete(null);
		waitingForCooldownGUI = false;
		sendCooldowns = true;
		onCityBuildJoin(null);
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
		for (Map.Entry<String, Long> entry : endDates.entrySet())
			o.addProperty(entry.getKey(), entry.getValue());

		// Save end dates along with player uuid so no problems occur when using multiple accounts
		Config.set("player.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId(), o);
		Config.save();
	}

	@EventListener
	public void loadCooldowns(GrieferGamesJoinEvent event) {
		String path = "player.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId();

		if (Config.has(path)) {
			endDates.clear();
			for (Map.Entry<String, JsonElement> e : Config.get(path).getAsJsonObject().entrySet()) {
				if (e.getKey().startsWith("/clan") || e.getKey().equals("Riesige GS überschreiben") || e.getKey().equals("/premium"))
					continue;

				endDates.put(e.getKey(), e.getValue().getAsLong());
			}
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
