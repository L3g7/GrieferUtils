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

package dev.l3g7.griefer_utils.features.modules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.misc.ServerCheck.isOnGrieferGames;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.ingamegui.enums.EnumModuleFormatting.SQUARE_BRACKETS;

@Singleton
public class Booster extends Module {

	private final static Pattern BOOSTER_INFO_PATTERN = Pattern.compile("^(?<name>[A-z]+)-Booster: (?:Deaktiviert|\\dx Multiplikator (?<durations>\\(.+\\) ?)+)");
	private final static Pattern BOOSTER_INFO_TIME_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
	private final static Pattern BOOSTER_ACTIVATE_PATTERN = Pattern.compile("^\\[Booster] .* hat für die GrieferGames Community den (?<name>.*)-Booster für 15 Minuten aktiviert\\.$");

	private final DropDownSetting<KeyMode> keyModeSetting = new DropDownSetting<>(KeyMode.class)
		.name("Design")
		.icon("wooden_board")
		.config("modules.booster.design")
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
	private String chatInput = null;

	public Booster() {
		super("Booster", "Zeigt dir die momentan aktiven Booster an", "booster", new IconData("griefer_utils/icons/rocket.png"));
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

		if (!(mc().currentScreen instanceof GuiChat)) {
			chatInput = null;
			return;
		}

		GuiTextField input = Reflection.get(mc().currentScreen, "inputField");
		chatInput = input == null ? null : input.getText();
	}

	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		if (!isOnGrieferGames())
			return;

		if (!(mc.currentScreen instanceof GuiChest))
			return;

		if (!getGuiChestTitle().equals("§6Booster - Übersicht§r"))
			return;

		if (waitingForBoosterGUI && isActive()) {
			player().closeScreen();
			if (chatInput != null)
				mc.displayGuiScreen(new GuiChat(chatInput));
			chatInput = null;
			waitingForBoosterGUI = false;
		}
	}

	@EventListener
	public void onMsg(MessageReceiveEvent event) {
		String msg = event.message.getUnformattedText();

		if (waitingForBoosterInfo && msg.equals("Folgende Booster sind auf diesem Server aktiv:")) {
			event.cancel();
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
			event.cancel();

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
			.map(BoosterData::getDisplayName)
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
			.map(BoosterData::getFormattedTime)
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

		if (keyModeSetting.get() == KeyMode.TEXT || !keyVisible)
			return;

		List<BoosterData> data = boosters.values().stream()
			.filter(d -> !d.isExpired())
			.collect(Collectors.toList());

		if (data.isEmpty())
			return;

		double singum = Math.signum(rightX);
		int fontHeight = mc.fontRendererObj.FONT_HEIGHT;

		double xDiff = 0;

		if (getDisplayFormatting() == SQUARE_BRACKETS)
			xDiff -= singum * mc.fontRendererObj.getStringWidth(new Text("[", 0, bold, italic, underline).getText());

		if (keyModeSetting.get() == KeyMode.ICON)
			xDiff += .5;

		// Add padding
		y += padding;
		if (rightX == -1)
			xDiff += padding;

		xDiff *= -singum;

		for (BoosterData d : data) {
			y += fontHeight + 1;

			double actualX = rightX == -1 ? x : rightX - getDisplayTextWidth(d);
			actualX += xDiff;

			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/" + d.displayName.toLowerCase() + ".png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(actualX, y, 256, 256, 7, 7);
		}

	}

	private double getDisplayTextWidth(BoosterData d) {
		List<Text> texts = getDisplayFormatting().getTexts(d.getDisplayName(), ImmutableList.of(new Text(d.getFormattedTime(), 0)), 0, 0, 0, keyVisible, bold, italic, underline);
		String text = texts.stream().map(Text::getText).reduce(String::concat).orElseThrow(() -> new RuntimeException("BoosterData has no text"));

		return mc.fontRendererObj.getStringWidth(text);
	}

	private class BoosterData {

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

		private String getFormattedTime() {
			return Util.formatTime(expirationDates.stream()
				.mapToLong(Long::longValue)
				.min().orElse(0));
		}

		private String getDisplayName() {
			KeyMode mode = keyModeSetting.get();
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