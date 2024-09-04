/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.countdowns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.HIDDEN;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;
import static net.labymod.ingamegui.enums.EnumModuleFormatting.SQUARE_BRACKETS;

@Singleton
public class PotionTimer extends Widget {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static final Pattern END_PATTERN = Pattern.compile("^§r§8\\[§r§6GrieferGames§r§8] §r§7Bis: §r§e(?<end>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})§r");

	private final Map<String, PotionData> potions = ImmutableMap.of(
			"break_potion", new PotionData("Break"),
			"fly_potion", new PotionData("Fly")
	);

	private final DropDownSetting<KeyMode> design = DropDownSetting.create(KeyMode.class)
			.name("Design")
			.description("In welchem die Design die aktivierten Tränke angezeigt werden sollen.")
			.icon("wooden_board")
			.defaultValue(KeyMode.TEXT_AND_ICON);

	private final NumberSetting warnTime = NumberSetting.create()
			.name("Warn-Zeit für Fly Tränke (s)")
			.description("Wie viele Sekunden vor dem Ablauf eines Fly-Tranks eine Warnung angezeigt werden soll.")
			.icon("labymod_3/exclamation_mark");

	private final SwitchSetting hide = SwitchSetting.create()
			.name("Verstecken, wenn nichts getrunken")
			.description("Ob das Modul versteckt werden soll, wenn derzeit kein Orbtrank aktiv ist.")
			.icon("blindness");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
			.name("Orbtrank-\nTimer")
			.description("Zeigt dir an, wie lange aktivierte Fly/Break Tränke noch anhalten.")
			.icon(Items.feather)
			.subSettings(design, warnTime, hide);

	private void checkFlyWarning() {
		// Warn if the fly potion end is less than the set amount of seconds away
		long flyPotionEnd = potions.get("fly_potion").expirationDate;
		if (flyPotionEnd > System.currentTimeMillis() && flyPotionEnd - System.currentTimeMillis() < warnTime.get() * 1000) {
			String s = Util.formatTime(flyPotionEnd, true);
			if (!s.equals("0s")) {
				mc().ingameGUI.displayTitle("§cFly Trank", null, -1, -1, -1);
				mc().ingameGUI.displayTitle(null, "§c§l" + s, -1, -1, -1);
				mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
			}
		}
	}

	@EventListener(triggerWhenDisabled = true)
	private void onMessage(MessageEvent.MessageReceiveEvent event) {
		Matcher matcher = END_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches())
			return;

		String end = matcher.group("end");
		try {
			potions.get("fly_potion").expirationDate = DATE_FORMAT.parse(end).getTime();
		} catch (ParseException e) {
			BugReporter.reportError(new Throwable("Error while parsing fly potion end from " + event.message.getFormattedText(), e));
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMouse(WindowClickEvent event) {
		if (!getGuiChestTitle().startsWith("§6Möchtest du den Trank benutzen?"))
			return;

		if (event.slotId != 12)
			return;

		ItemStack heldItem = player().getHeldItem();
		if (heldItem == null || !heldItem.hasTagCompound())
			return;

		NBTTagCompound tag = heldItem.getTagCompound();
		for (Map.Entry<String, PotionData> entry : potions.entrySet()) {
			if (tag.hasKey(entry.getKey())) {
				entry.getValue().expirationDate = System.currentTimeMillis() + 15 * 60 * 1000;
				break;
			}
		}
	}

	@Override
	protected LabyWidget getLaby3() {
		return new PotionTimerL3();
	}

	@Override
	protected LabyWidget getLaby4() {
		return new PotionTimerL4();
	}

	private static class PotionData {

		private final String displayName;
		private long expirationDate = -1;

		public PotionData(String displayName) {
			this.displayName = displayName;
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
	private class PotionTimerL3 extends Laby3Widget {

		@Override
		public boolean isShown() {
			return super.isShown() && (!hide.get() || potions.values().stream().anyMatch(p -> p.expirationDate >= System.currentTimeMillis()));
		}

		@Override
		public String[] getKeys() {
			if (!ServerCheck.isOnCitybuild())
				return new String[] {"Orbtrank-Timer"};

			// Get names as Strings
			List<String> keys = potions.values().stream()
					.filter(d -> d.expirationDate >= System.currentTimeMillis())
					.map(this::getDisplayName)
					.collect(Collectors.toList());

			keys.add(0, "Orbtrank-Timer");
			return keys.toArray(new String[0]);
		}

		@Override
		public String[] getDefaultValues() {
			return new String[] {""};
		}

		@Override
		public String[] getValues() {
			if (!ServerCheck.isOnCitybuild())
				return getDefaultValues();

			// Get expirations dates as Strings
			List<String> values = potions.values().stream()
					.filter(d -> d.expirationDate >= System.currentTimeMillis())
					.map(this::getFormattedTime)
					.collect(Collectors.toList());

			if (values.isEmpty())
				return getDefaultValues();

			checkFlyWarning();
			values.add(0, "");
			return values.toArray(new String[0]);
		}

		@Override
		public void draw(double x, double y, double rightX) {
			super.draw(x, y, rightX);

			if (!ServerCheck.isOnCitybuild())
				return;

			if (design.get() == KeyMode.TEXT || !keyVisible)
				return;

			List<PotionData> data = potions.values().stream()
					.filter(d -> d.expirationDate >= System.currentTimeMillis())
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

			for (PotionData d : data) {
				y += fontHeight + 1;

				double actualX = rightX == -1 ? x : rightX - getDisplayTextWidth(d);
				actualX += xDiff;

				mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/" + d.displayName.toLowerCase() + ".png"));
				LabyMod.getInstance().getDrawUtils().drawTexture(actualX, y, 256, 256, 7, 7);
			}

		}

		private double getDisplayTextWidth(PotionData d) {
			List<Text> texts = getDisplayFormatting().getTexts(getDisplayName(d), ImmutableList.of(new Text(getFormattedTime(d), 0)), 0, 0, 0, keyVisible, bold, italic, underline);
			String text = texts.stream().map(Text::getText).reduce(String::concat).orElseThrow(() -> new RuntimeException("PotionData has no text"));

			return mc.fontRendererObj.getStringWidth(text);
		}

		private String getFormattedTime(PotionData data) {
			return Util.formatTime(data.expirationDate);
		}

		private String getDisplayName(PotionData data) {
			KeyMode mode = design.get();
			String name = "";

			if (mode != KeyMode.TEXT)
				name += "  "; // Space for the icon
			if (mode != KeyMode.ICON)
				name += data.displayName;

			return name;
		}

	}

	@ExclusiveTo(LABY_4)
	private class PotionTimerL4 extends Laby4Widget {

		private final List<PotionLine> lines = List.of(
				new PotionLine(potions.get("break_potion")),
				new PotionLine(potions.get("fly_potion"))
		);

		@Override
		public boolean isVisibleInGame() {
			return !hide.get() || potions.values().stream().anyMatch(p -> p.expirationDate >= System.currentTimeMillis());
		}

		@Override
		protected void createText() {
			createLine("Orbtrank-Timer", "");
			lines.forEach(PotionLine::createLine);
		}

		@Override
		public void onTick(boolean isEditorContext) {
			lines.forEach(PotionLine::tick);
			checkFlyWarning();
		}

		@ExclusiveTo(LABY_4)
		private class PotionLine {

			private final PotionData data;
			private final Component keyComponent = Component.empty();
			private TextLine line;

			private PotionLine(PotionData data) {
				this.data = data;
			}

			private void createLine() {
				line = PotionTimerL4.this.createLine(keyComponent, "");
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
				if (data.expirationDate < System.currentTimeMillis()) {
					line.setState(HIDDEN);
					line.updateAndFlush("00:00"); // Fallback value for showcase in Widget editor | FIXME: tick down?
				} else {
					line.setState(VISIBLE);
					line.updateAndFlush(Util.formatTime(data.expirationDate));
				}

				reinitialize();
			}

		}

	}

}
