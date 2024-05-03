/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.spawn_counter;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class SpawnCounter extends Laby4Module {

	final LeaderboardHandler leaderboardHandler = new LeaderboardHandler(this);
	final RoundHandler roundHandler = new RoundHandler(this);

	static final String configKey = "modules.spawn_counter.rounds_";

	final DropDownSetting<NotificationType> notificationType = DropDownSetting.create(NotificationType.class)
		.name("Nachricht")
		.description("Wie die Benachrichtung aussehen soll, wenn eine Runde abgeschlossen wurde.")
		.icon(Items.clock)
		.defaultValue(NotificationType.ACTIONBAR);

	private final DropDownSetting<RoundDisplayType> displayType = DropDownSetting.create(RoundDisplayType.class)
		.name("Rundenart")
		.description("Welche Arten von Runden angezeigt werden sollen.")
		.icon("speed")
		.defaultValue(RoundDisplayType.BOTH);

	final DropDownSetting<LeaderboardDisplayType> leaderboard = DropDownSetting.create(LeaderboardDisplayType.class)
		.name("Leaderboard")
		.description("Das Aussehen des Leaderboards.\nBei §oAus§r wird auch die Teilnahme am Leaderboard deaktiviert.")
		.icon("trophy")
		.defaultValue(LeaderboardDisplayType.ON)
		.callback(t -> {
			if (t != LeaderboardDisplayType.OFF && ServerCheck.isOnGrieferGames() && leaderboardHandler.data != null)
				leaderboardHandler.onGrieferGamesJoin(null);
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawn-Runden Zähler")
		.description("Zählt, wie viele Runden um den Spawn gelaufen wurden.")
		.icon("speed")
		.subSettings(notificationType, displayType, leaderboard, HeaderSetting.create());

	public SpawnCounter() {
		if (Config.has(configKey + "flown"))
			roundHandler.roundsFlown = Config.get(configKey + "flown").getAsInt();
		if (Config.has(configKey + "ran"))
			roundHandler.roundsRan = Config.get(configKey + "ran").getAsInt();
	}

	@Override
	protected void createText() {
		super.createText();
		leaderboardHandler.createLines();
	}

	@Override
	public void onTick(boolean isEditorContext) {
		super.onTick(isEditorContext);
		leaderboardHandler.tickLines();
	}

	@Override
	public Object getValue() {
		Component value = Component.empty();

		if (displayType.get() != RoundDisplayType.FLOWN)
			value.append(Component.icon(new OffsetIcon(SettingsImpl.buildIcon("speed"), -2, -1), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT))
				.append(Component.text(roundHandler.roundsRan));

		if (displayType.get() == RoundDisplayType.BOTH)
			value.append(Component.text(" "));

		if (displayType.get() != RoundDisplayType.RAN)
			value.append(Component.icon(new OffsetIcon(SettingsImpl.buildIcon("booster/fly"), -2, -1), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT)) // NOTE: cleanup
				.append(Component.text(roundHandler.roundsFlown));

		if (leaderboard.get() == LeaderboardDisplayType.COMPACT)
			value.append(Component.text(" ┃ " + leaderboardHandler.data.position + ".: " + leaderboardHandler.data.score));

		return value;
	}

	void addLine(TextLine textLine) {
		lines.add(textLine);
	}

	@Override
	public String getComparisonName() {
		return "dev.l3g7.griefer_utils.v1_8_9.features.modules" + enabled.name();
	}

	enum NotificationType implements Named {

		NONE("Keine", s -> {}),
		TOAST("Erfolg", s -> LabyBridge.labyBridge.notify("§aSpawn-Runden Zähler", s)),
		ACTIONBAR("Aktionsleiste", s -> mc().ingameGUI.setRecordPlaying(s, true)),
		MESSAGE("Chatnachricht", s -> display(Constants.ADDON_PREFIX + s));

		private final String name;
		public final Consumer<String> notifier;

		NotificationType(String name, Consumer<String> notifier) {
			this.name = name;
			this.notifier = notifier;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	private enum RoundDisplayType implements Named {

		RAN("Laufen"),
		FLOWN("Fliegen"),
		BOTH("Beides");

		private final String name;

		RoundDisplayType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	enum LeaderboardDisplayType implements Named {

		OFF("Aus"),
		INVISIBLE("Unsichtbar"),
		COMPACT("Kompakt"),
		ON("An");

		private final String name;

		LeaderboardDisplayType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
