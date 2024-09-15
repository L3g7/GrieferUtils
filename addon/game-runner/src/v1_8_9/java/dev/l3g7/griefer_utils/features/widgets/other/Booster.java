/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.core.misc.TPSCountdown;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Commands;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.HIDDEN;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;
import static net.labymod.ingamegui.enums.EnumModuleFormatting.SQUARE_BRACKETS;

@Singleton
public class Booster extends Widget {

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

	private final DropDownSetting<KeyMode> design = DropDownSetting.create(KeyMode.class)
		.name("Design")
		.description("In welchem Design die derzeit aktiven Booster angezeigt werden sollen.")
		.icon("wooden_board")
		.config("modules.booster.design")
		.defaultValue(KeyMode.TEXT_AND_ICON);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Booster")
		.description("Zeigt dir die momentan aktiven Booster an.")
		.icon("rocket")
		.subSettings(design);

	private boolean waitingForBoosterGUI = false;
	private boolean waitingForBoosterInfo = false;

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		// Clear all booster
		boosters.values().forEach(d -> d.expirationDates.clear());
	}

	@EventListener
	private void onCbEarlyJoin(CitybuildJoinEvent.Early event) {
		Commands.runOnCb("/booster");
		waitingForBoosterGUI = waitingForBoosterInfo = true;
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

	@EventListener(triggerWhenDisabled = true)
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
			Queue<TPSCountdown> dates = booster.expirationDates;

			if (booster.stackable) {
				dates.add(TPSCountdown.fromMinutes(15));
				return;
			}

			if (dates.isEmpty())
				dates.add(TPSCountdown.fromMinutes(15));
			else
				dates.peek().addMinutes(15);

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
		Queue<TPSCountdown> expirationDates = boosters.get(name).expirationDates;
		expirationDates.clear();

		if (durations == null)
			return;

		m = BOOSTER_INFO_TIME_PATTERN.matcher(durations);
		while (m.find()) {
			int min = Integer.parseInt(m.group(1));
			int sek = Integer.parseInt(m.group(2));

			expirationDates.add(TPSCountdown.fromSeconds(min * 60 + sek));
		}

	}

	@Override
	protected LabyWidget getLaby3() {
		return new BoosterL3();
	}

	@Override
	protected LabyWidget getLaby4() {
		return new BoosterL4();
	}

	static class BoosterData {

		final String displayName;
		final boolean stackable;
		final Queue<TPSCountdown> expirationDates = new ConcurrentLinkedQueue<>();

		public BoosterData(String displayName, boolean stackable) {
			this.displayName = displayName;
			this.stackable = stackable;
		}

		private boolean isExpired() {
			expirationDates.removeIf(TPSCountdown::isExpired);
			return expirationDates.isEmpty();
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

	@ExclusiveTo(LABY_3)
	public class BoosterL3 extends Laby3Widget {

		@Override
		public String getComparisonName() {
			String pkg = getClass().getPackage().getName();
			pkg = pkg.substring(0, pkg.lastIndexOf("."));
			return pkg + "." + getControlName();
		}

		@Override
		public String[] getKeys() {
			// Get names (and counts) as Strings
			List<String> keys = boosters.values().stream()
				.filter(d -> !d.isExpired())
				.map(this::getDisplayName)
				.collect(Collectors.toList());

			keys.add(0, "Booster");
			return keys.toArray(new String[0]);
		}

		@Override
		public String[] getDefaultValues() {
			return new String[]{"0"};
		}

		@Override
		public String[] getValues() {
			// Get expirations dates as Strings
			List<String> values = boosters.values().stream()
				.filter(d -> !d.isExpired())
				.map(this::getFormattedTime)
				.collect(Collectors.toList());

			if (values.isEmpty())
				return getDefaultValues();

			// Add count to start
			values.add(0, String.valueOf(boosters.values().stream()
				.map(d -> d.expirationDates.size())
				.mapToInt(Integer::intValue)
				.sum()));

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

			double signum = Math.signum(rightX);
			int fontHeight = mc.fontRendererObj.FONT_HEIGHT;

			double xDiff = 0;

			if (getDisplayFormatting() == SQUARE_BRACKETS)
				xDiff -= signum * mc.fontRendererObj.getStringWidth(new Text("[", 0, bold, italic, underline).getText());

			if (design.get() == KeyMode.ICON)
				xDiff += .5;

			// Add padding
			y += padding;
			if (rightX == -1)
				xDiff += padding;

			xDiff *= -signum;

			for (BoosterData d : data) {
				y += fontHeight + 1;

				double actualX = rightX == -1 ? x : rightX - getDisplayTextWidth(d);
				actualX += xDiff;

				mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/" + d.displayName.toLowerCase() + ".png"));
				LabyMod.getInstance().getDrawUtils().drawTexture(actualX, y, 256, 256, 7, 7);
			}

		}

		private double getDisplayTextWidth(BoosterData d) {
			List<Text> texts = getDisplayFormatting().getTexts(getDisplayName(d), ImmutableList.of(new Text(getFormattedTime(d), 0)), 0, 0, 0, keyVisible, bold, italic, underline);
			String text = texts.stream().map(Text::getText).reduce(String::concat).orElseThrow(() -> new RuntimeException("BoosterData has no text"));

			return mc.fontRendererObj.getStringWidth(text);
		}

		private String getFormattedTime(BoosterData data) {
			return Util.formatTimeSeconds(data.expirationDates.stream()
				.mapToLong(TPSCountdown::secondsRemaining)
				.min().orElse(0));
		}

		private String getDisplayName(BoosterData data) {
			KeyMode mode = design.get();
			String name = "";

			if (mode != KeyMode.TEXT)
				name += "  "; // Space for the icon
			if (mode != KeyMode.ICON)
				name += data.displayName;
			if (data.stackable)
				name += " x" + data.expirationDates.size();

			return name;
		}

	}

	@ExclusiveTo(LABY_4)
	private class BoosterL4 extends Laby4Widget {

		private final List<BoosterLine> lines = boosters.values().stream().map(BoosterLine::new).toList();

		@Override
		protected void createText() {
			super.createText();
			lines.forEach(BoosterLine::createLine);
		}

		@Override
		public void onTick(boolean isEditorContext) {
			lines.forEach(BoosterLine::tick);
		}

		@Override
		public Object getValue() {
			return String.valueOf(boosters.values().stream()
				.map(data -> data.expirationDates.size())
				.mapToInt(Integer::intValue).sum());
		}

		@Override
		public String getComparisonName() {
			return "dev.l3g7.griefer_utils.features.widgets" + enabled.name();
		}

		@ExclusiveTo(LABY_4)
		private class BoosterLine {

			private final BoosterData data;
			private final Component keyComponent = Component.empty();
			private TextLine line;

			public BoosterLine(BoosterData data) {
				this.data = data;
			}

			private void createLine() {
				line = BoosterL4.this.createLine(keyComponent, "");
			}

			private void tick() {
				// Update key
				KeyMode mode = design.get();
				Component name = Component.empty();

				if (mode != KeyMode.TEXT) {
					if (mode == KeyMode.ICON)
						name.append(Component.icon(Icons.of("booster/" + data.displayName.toLowerCase(), -0.5f, -.5f), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT));
					else
						// Text and icon
						name.append(Component.icon(Icons.of("booster/" + data.displayName.toLowerCase(), .5f, -1), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT));
				}

				if (mode == KeyMode.TEXT_AND_ICON)
					name.append(Component.text(" "));

				if (mode != KeyMode.ICON)
					name.append(Component.text(data.displayName));

				keyComponent.setChildren(Collections.singleton(name));

				// Update value
				if (data.isExpired()) {
					line.setState(HIDDEN);
					line.updateAndFlush("00:00"); // Fallback value for showcase in Widget editor | FIXME: tick down? fix booster count?
				} else {
					line.setState(VISIBLE);
					line.updateAndFlush(Util.formatTimeSeconds(data.expirationDates.stream()
						.mapToLong(TPSCountdown::secondsRemaining)
						.min().orElse(0)));
				}

				reinitialize();
			}

		}

	}

}
