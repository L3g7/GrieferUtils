/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.booster;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Commands;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.*;

@Singleton
public class Booster extends Laby4Module {

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

	public Booster() {
		Runtime.getRuntime().addShutdownHook(new Thread(BoosterRequestHandler::deleteBoosterData));
	}

	@Override
	protected void createText() {
		super.createText();
		boosters.values().forEach(BoosterData::createLine);
	}

	@Override
	public void onTick(boolean isEditorContext) {
		boosters.values().forEach(BoosterData::tick);
	}

	@Override
	public Object getValue() {
		return String.valueOf(boosters.values().stream()
			.map(data -> data.expirationDates.size())
			.mapToInt(Integer::intValue).sum());
	}

	@Override
	public String getComparisonName() {
		return "dev.l3g7.griefer_utils.v1_8_9.features.modules" + enabled.name();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		// Clear all booster
		boosters.values().forEach(d -> d.expirationDates.clear());
		BoosterRequestHandler.deleteBoosterData();
	}

	@EventListener(triggerWhenDisabled = true)
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

	class BoosterData {

		private final Component keyComponent = Component.empty();
		private TextLine line;
		final String displayName;
		final boolean stackable;
		final Queue<Long> expirationDates = new ConcurrentLinkedQueue<>();

		public BoosterData(String displayName, boolean stackable) {
			this.displayName = displayName;
			this.stackable = stackable;
		}

		private void createLine() {
			line = Booster.this.createLine(keyComponent, "");
		}

		private void tick() {

			// Update key
			KeyMode mode = design.get();
			Component name = Component.empty();

			if (mode != KeyMode.TEXT) {
				if (mode == KeyMode.ICON)
					name.append(Component.icon(new OffsetIcon(SettingsImpl.buildIcon("booster/" + displayName.toLowerCase()), -0.5f, -.5f), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT));
				else
					// Text and icon
					name.append(Component.icon(new OffsetIcon(SettingsImpl.buildIcon("booster/" + displayName.toLowerCase()), .5f, -1), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT));
			}

			if (mode == KeyMode.TEXT_AND_ICON)
				name.append(Component.text(" "));

			if (mode != KeyMode.ICON)
				name.append(Component.text(displayName));

			keyComponent.setChildren(Collections.singleton(name));

			// Update value
			long currentTime = System.currentTimeMillis();
			expirationDates.removeIf(d -> d < currentTime);

			if (expirationDates.isEmpty()) {
				line.setState(HIDDEN);
				line.update("00:00"); // Fallback value for showcase in Widget editor | FIXME: tick down? fix booster count?
			} else {
				line.setState(VISIBLE);
				line.update(Util.formatTime(expirationDates.stream()
					.mapToLong(Long::longValue)
					.min().orElse(0)));
			}

			reinitialize();
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