/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.misc.ChatQueue;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

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
		.description("Zeigt die momentanen Cooldowns an.")
		.icon(Material.WATCH)
		.callback(v -> {
			// If no data is found, open and close /cooldowns automatically
			if (v && endDates.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForCooldownGUI) {
				guiInitBlock = ChatQueue.sendBlocking("/cooldowns", () -> {
					LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("§c§lFehler \u26A0", "§c" + "/cooldowns geht nicht!");
					resetWaitingForGUI();
				});
				waitingForCooldownGUI = true;
			}
		});

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().matches("^Du hast .+-Booster erhalten\\. Danke für deine Unterstützung von GrieferGames!$"))
			endDates.put("/grieferboost", System.currentTimeMillis() + 1000 * 3600 * 24 * 14);
		else if (event.message.getUnformattedText().equals("[CaseOpening] Du hast 2 Kisten erhalten."))
			endDates.put("/freekiste", System.currentTimeMillis() + 1000 * 3600 * 24 * 14);
		else if (event.message.getUnformattedText().matches("^\\[Kopf] Du hast einen .+ Kopf erhalten[!.]$"))
			endDates.put("/kopf", System.currentTimeMillis() + 1000 * 3600 * 24 * (PlayerUtil.getRank(PlayerUtil.getName()).equals("Titan") ? 14 : 7));
		else if (event.message.getUnformattedText().matches("^Du hast .+ den Premium Rang aktiviert\\.$"))
			endDates.put("/premium", System.currentTimeMillis() + 1000 * 3600 * 24 * 7);
		else if (event.message.getUnformattedText().equals("[StartKick] Ersteller: " + PlayerUtil.getName()))
			endDates.put("/startkick", System.currentTimeMillis() + 1000 * 3600 * 12);
		else
			return;

		saveCooldowns();
	}

	@EventListener
	public void onCBJoin(CityBuildJoinEvent event) {
		// If no data is found, open and close /cooldowns automatically
		if (endDates.isEmpty()) {
			guiInitBlock = ChatQueue.sendBlocking("/cooldowns", () -> {
				LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("§c§lFehler \u26A0", "§c" + "/cooldowns geht nicht!");
				resetWaitingForGUI();
			});
			waitingForCooldownGUI = true;
		}
	}

	@EventListener(priority = EventPriority.LOWEST) // Make sure loadCooldowns is triggered before
	public void onServerJoin(ServerEvent.ServerJoinEvent event) {
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
			: s -> LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(Constants.ADDON_NAME, "§e%s ist nun §averfügbar§e!", s);

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

					try {
						endDates.put(name, getAvailability(s));
					} catch (ParseException e) {
						// Ignore item
						e.printStackTrace();
						continue;
					}
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
		Config.set("features.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId(), o);
		Config.save();
	}

	@EventListener
	public void loadCooldowns(ServerEvent.ServerJoinEvent event) {
		String path = "features.cooldown_notifications.end_dates." + mc().getSession().getProfile().getId();

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
	private static long getAvailability(ItemStack i) throws ParseException {
		NBTTagList lore = i.serializeNBT().getCompoundTag("tag").getCompoundTag("display").getTagList("Lore", NBT.TAG_STRING);
		if (lore.tagCount() == 1) {
			if (lore.getStringTagAt(0).equals("§aVerfügbar"))
				return 0;

			return -1;
		} else if (lore.tagCount() == 2) {
			String dateStr = lore.getStringTagAt(1)
				.replace("§7am §e§e", "")
				.replace(" §7um§e ", " ")
				.replace(" §7frei.", "");
			return DATE_FORMAT.parse(dateStr).getTime();
		}
		return -2;
	}

}
