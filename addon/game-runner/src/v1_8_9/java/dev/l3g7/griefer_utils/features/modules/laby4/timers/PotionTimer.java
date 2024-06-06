/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4.timers;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.labymod.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.HIDDEN;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;

@Singleton
@ExclusiveTo(LABY_4)
public class PotionTimer extends Laby4Module {

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

	@Override
	public boolean isVisibleInGame() {
		return !hide.get() || potions.values().stream().anyMatch(p -> p.expirationDate >= System.currentTimeMillis());
	}

	@Override
	protected void createText() {
		createLine("Orbtrank-Timer", "");
		potions.values().forEach(PotionData::createLine);
	}

	@Override
	public void onTick(boolean isEditorContext) {
		potions.values().forEach(PotionData::tick);

		// Warn if the fly potion end is less than the set amount of seconds away
		long flyPotionEnd = potions.get("fly_potion").expirationDate;
		if (flyPotionEnd > System.currentTimeMillis() && flyPotionEnd - System.currentTimeMillis() < warnTime.get() * 1000) {
			String s = Util.formatTime(flyPotionEnd, true);
			if (!s.equals("0s"))
				title("§c§l" + s);
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

	private void title(String title) {
		mc().ingameGUI.displayTitle("§cFly Trank", null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, title, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
	}

	private class PotionData {

		private final Component keyComponent = Component.empty();
		private TextLine line;
		private final String displayName;
		private long expirationDate = -1;

		private PotionData(String displayName) {
			this.displayName = displayName;
		}

		private void createLine() {
			line = PotionTimer.this.createLine(keyComponent, "");
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
			if (expirationDate < System.currentTimeMillis()) {
				line.setState(HIDDEN);
				line.update("00:00"); // Fallback value for showcase in Widget editor | FIXME: tick down?
			} else {
				line.setState(VISIBLE);
				line.update(Util.formatTime(expirationDate));
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
