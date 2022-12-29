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

package dev.l3g7.griefer_utils.features.modules;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.Util;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.misc.TickScheduler;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.misc.ServerCheck.isOnGrieferGames;

@Singleton
public class Booster extends Module {

	private final static Pattern BOOSTER_INFO_PATTERN = Pattern.compile("^(?<name>[A-z]+)-Booster: (?:Deaktiviert|\\dx Multiplikator (?<durations>\\(.+\\) ?)+)");
	private final static Pattern BOOSTER_INFO_TIME_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
	private final static Pattern BOOSTER_ACTIVATE_PATTERN = Pattern.compile("^\\[Booster] .* hat für die GrieferGames Community den (?<name>.*)-Booster für 15 Minuten aktiviert\\.$");

	private final DropDownSetting<KeyMode> keyModeSetting = new DropDownSetting<>(KeyMode.class)
		.name("Bezeichner")
		.config("modules.booster.key_mode")
		.defaultValue(KeyMode.TEXT_AND_ICON)
		.stringProvider(KeyMode::getName);

	private final Map<String, BoosterData> boosters = ImmutableMap.of(
		"Break", new BoosterData("Break", false),
		"Drops", new BoosterData("Drop", true),
		"Fly", new BoosterData("Fly", false),
		"Mob", new BoosterData("Mob", true),
		"Erfahrung", new BoosterData("XP", true)
	);

	private boolean waitingForBoosterGUI = false;
	private boolean waitingForBoosterInfo = false;

	public Booster() {
		super("Booster", "Zeigt dir die momentan aktiven Booster an", "booster", new IconData(Material.FIREWORK));
	}

	@EventListener
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		// Clear all booster
		boosters.values().forEach(d -> d.expirationDates.clear());
	}

	@EventListener
	public void onCBJoin(CityBuildJoinEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		MinecraftUtil.send("/booster");
		waitingForBoosterGUI = waitingForBoosterInfo = true;
	}

	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		if (!isOnGrieferGames())
			return;

		if (!(mc.currentScreen instanceof GuiChest))
			return;

		IInventory inventory = Reflection.get(mc.currentScreen, "lowerChestInventory");

		if (!inventory.getDisplayName().getFormattedText().equals("§6Booster - Übersicht§r"))
			return;

		if (waitingForBoosterGUI && isActive()) {
			mc.displayGuiScreen(null);
			waitingForBoosterGUI = false;
		}
	}

	@EventListener
	public void onMsg(ClientChatReceivedEvent event) {
		String msg = event.message.getUnformattedText();

		if (waitingForBoosterInfo && msg.equals("Folgende Booster sind auf diesem Server aktiv:")) {
			event.setCanceled(true);
			TickScheduler.runAfterClientTicks(() -> waitingForBoosterInfo = false, 1);
			return;
		}

		// Check for activation
		Matcher m = BOOSTER_ACTIVATE_PATTERN.matcher(msg);
		if (m.matches()) {
			String name = m.group("name");

			BoosterData booster = boosters.get(name);
			List<Long> dates = booster.expirationDates;

			if (booster.stackable) {
				dates.add(System.currentTimeMillis() + 15 * 60 * 1000);
				return;
			}

			if (dates.isEmpty())
				dates.add(System.currentTimeMillis() + 15 * 60 * 1000);
			else
				dates.set(0, dates.get(0) + 15 * 60 * 1000);
			return;
		}

		// Check for info
		m = BOOSTER_INFO_PATTERN.matcher(msg);
		if (!m.matches())
			return;

		if (waitingForBoosterInfo)
			event.setCanceled(true);

		String name = m.group("name");
		String durations = m.group("durations");
		List<Long> expirationDates = boosters.get(name).expirationDates;
		expirationDates.clear();

		if (durations == null)
			return;

		m = BOOSTER_INFO_TIME_PATTERN.matcher(durations);
		while (m.find()) {
			int min = Integer.parseInt(m.group(1));
			int sek = Integer.parseInt(m.group(2));

			int ms = (min * 60 + sek) * 1000;
			expirationDates.add(System.currentTimeMillis() + ms);
		}
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(keyModeSetting);
	}

	@Override
	public String[] getKeys() {
		// Get names (and counts) as Strings
		List<String> keys = boosters.values().stream()
			.filter(d -> !d.isExpired())
			.map(d -> d.getDisplayName(keyModeSetting.get()))
			.collect(Collectors.toList());

		keys.add(0, "Booster");
		return keys.toArray(new String[0]);
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] {"0"};
	}

	@Override
	public String[] getValues() {
		if (!ServerCheck.isOnCitybuild())
			return getDefaultValues();

		// Get expirations dates as Strings
		List<String> values = boosters.values().stream()
			.filter(d -> !d.isExpired())
			.map(d -> Util.formatTime(d.expirationDates.stream().mapToLong(Long::longValue).min().orElse(0)))
			.collect(Collectors.toList());

		if (values.isEmpty())
			return getDefaultValues();

		// Add count to start
		values.add(0, String.valueOf(boosters.values().stream().map(BoosterData::count).mapToInt(Integer::intValue).sum()));

		return values.toArray(new String[0]);
	}

	@Override
	public void draw(double x, double y, double rightX) {
		super.draw(x, y, rightX);

		if (keyModeSetting.get() == KeyMode.TEXT)
			return;

		List<BoosterData> data = boosters.values().stream()
			.filter(d -> !d.isExpired())
			.collect(Collectors.toList());

		if (data.isEmpty())
			return;

		int fontHeight = mc.fontRendererObj.FONT_HEIGHT;
		int bracketWidth = mc.fontRendererObj.getCharWidth('[');
		x += bracketWidth;

		if (keyModeSetting.get() == KeyMode.ICON)
			x += .5;

		for (BoosterData d : data) {
			y += fontHeight + 1;
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/" + d.displayName.toLowerCase() + ".png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(x, y, 256, 256, 7, 7);
		}
	}

	private static class BoosterData {

		private final String displayName;
		private final boolean stackable;
		private final List<Long> expirationDates = new ArrayList<>();

		public BoosterData(String displayName, boolean stackable) {
			this.displayName = displayName;
			this.stackable = stackable;
		}

		private boolean isExpired() {
			long currentTime = System.currentTimeMillis();
			expirationDates.removeIf(d -> d < currentTime);

			return expirationDates.isEmpty();
		}

		private String getDisplayName(KeyMode mode) {
			String name = "";

			if (mode != KeyMode.TEXT)
				name += "  "; // Space for the icon
			if (mode != KeyMode.ICON)
				name += displayName;
			if (stackable)
				name += " x" + count();

			return name;
		}

		private int count() {
			return expirationDates.size();
		}

	}

	private enum KeyMode {
		ICON("Icon"),
		TEXT("Text"),
		TEXT_AND_ICON("Text & Icon");

		private final String name;

		KeyMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}