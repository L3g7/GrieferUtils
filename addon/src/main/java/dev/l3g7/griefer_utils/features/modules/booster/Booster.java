/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.booster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Commands;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.labymod.ingamegui.enums.EnumModuleFormatting.SQUARE_BRACKETS;

@Singleton
public class Booster extends Module {

	private final static Pattern BOOSTER_INFO_PATTERN = Pattern.compile("^(?<name>[A-z]+)-Booster: (?:Deaktiviert|\\dx Multiplikator (?<durations>\\(.+\\) ?)+)");
	private final static Pattern BOOSTER_INFO_TIME_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
	private final static Pattern BOOSTER_ACTIVATE_PATTERN = Pattern.compile("^\\[Booster] .* hat für die GrieferGames Community den (?<name>.*)-Booster für 15 Minuten aktiviert\\.$");

	private final Map<String, BoosterData> boosters = ImmutableMap.of(
		"Break", new BoosterData("Break", false),
		"Drops", new BoosterData("Drop", true),
		"Fly", new BoosterData("Fly", false),
		"Mob", new BoosterData("Mob", true),
		"Erfahrung", new BoosterData("XP", true)
	);

	private final DropDownSetting<KeyMode> design = new DropDownSetting<>(KeyMode.class)
		.name("Design")
		.description("In welchem Design die derzeit aktiven Booster angezeigt werden sollen.")
		.icon("wooden_board")
		.config("modules.booster.design")
		.defaultValue(KeyMode.TEXT_AND_ICON)
		.stringProvider(KeyMode::getName);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Booster")
		.description("Zeigt dir die momentan aktiven Booster an.")
		.icon("rocket")
		.subSettings(design);

	private boolean waitingForBoosterGUI = false;
	private boolean waitingForBoosterInfo = false;

	@Override
	public void init() {
		super.init();
		Runtime.getRuntime().addShutdownHook(new Thread(BoosterRequestHandler::deleteBoosterData));
	}

	@Override
	public String getComparisonName() {
		return "dev.l3g7.griefer_utils.features.modules" + getControlName();
	}

	@EventListener
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		// Clear all booster
		boosters.values().forEach(d -> d.expirationDates.clear());
		BoosterRequestHandler.deleteBoosterData();
	}

	@EventListener
	private void onServerQuit(ServerEvent.ServerQuitEvent event) {
		BoosterRequestHandler.deleteBoosterData();
	}

	@EventListener
	private void onCbEarlyJoin(CitybuildJoinEvent.Early event) {
		BoosterRequestHandler.requestBoosterData(event.citybuild, boosters.values(), () -> {
			Commands.runOnCb("/booster");
			waitingForBoosterGUI = waitingForBoosterInfo = true;
		});
	}

	@EventListener
	private void onGuiChestOpen(GuiOpenEvent<GuiChest> event) {
		if (!waitingForBoosterGUI)
			return;

		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		if (!lowerChestInventory.getDisplayName().getFormattedText().equals("§6Booster - Übersicht§r"))
			return;

		event.cancel();
		waitingForBoosterGUI = false;
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
			Queue<Long> dates = booster.expirationDates;

			if (booster.stackable) {
				dates.add(System.currentTimeMillis() + 15 * 60 * 1000);
				BoosterRequestHandler.sendBoosterData(boosters.values());
				return;
			}

			if (dates.isEmpty())
				dates.add(System.currentTimeMillis() + 15 * 60 * 1000);
			else
				dates.add(dates.poll() + 15 * 60 * 1000);

			BoosterRequestHandler.sendBoosterData(boosters.values());
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
		Queue<Long> expirationDates = boosters.get(name).expirationDates;
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

		BoosterRequestHandler.sendBoosterData(boosters.values());
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

		if (design.get() == KeyMode.TEXT || !keyVisible)
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

		if (design.get() == KeyMode.ICON)
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

	class BoosterData {

		final String displayName;
		final boolean stackable;
		final Queue<Long> expirationDates = new ConcurrentLinkedQueue<>();

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
			KeyMode mode = design.get();
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

	private enum KeyMode implements Named {
		ICON("Icon"),
		TEXT("Text"),
		TEXT_AND_ICON("Text & Icon");

		private final String name;

		KeyMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}