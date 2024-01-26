/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class SpawnCounter extends Module {

	static final String configKey = "modules.spawn_counter.rounds_";

	private final DropDownSetting<NotificationType> notificationType = DropDownSetting.create(NotificationType.class)
		.name("Nachricht")
		.description("Wie die Benachrichtung aussehen soll, wenn eine Runde abgeschlossen wurde.")
		.icon(Items.clock)
		.defaultValue(NotificationType.ACTIONBAR);

	private final DropDownSetting<RoundDisplayType> displayType = DropDownSetting.create(RoundDisplayType.class)
		.name("Rundenart")
		.description("Welche Arten von Runden angezeigt werden sollen.")
		.icon("speed")
		.defaultValue(RoundDisplayType.BOTH);

	static final DropDownSetting<LeaderboardDisplayType> leaderboard = DropDownSetting.create(LeaderboardDisplayType.class)
		.name("Leaderboard")
		.description("Das Aussehen des Leaderboards.\nBei §oAus§r wird auch die Teilnahme am Leaderboard deaktiviert.")
		.icon("trophy")
		.defaultValue(LeaderboardDisplayType.ON)
		.callback(t -> {
			if (t != LeaderboardDisplayType.OFF && ServerCheck.isOnGrieferGames() && LeaderboardHandler.data != null)
				LeaderboardHandler.onGrieferGamesJoin(null);
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawn-Runden Zähler")
		.description("Zählt, wie viele Runden um den Spawn gelaufen wurden.")
		.icon("speed")
		.subSettings(notificationType, displayType, leaderboard, HeaderSetting.create());

	public SpawnCounter() {
		if (Config.has(configKey + "flown"))
			RoundHandler.roundsFlown = Config.get(configKey + "flown").getAsInt();
		if (Config.has(configKey + "ran"))
			RoundHandler.roundsRan = Config.get(configKey + "ran").getAsInt();

		LeaderboardHandler.sc = this;
	}

	@Override
	public String getComparisonName() {
		return "dev.l3g7.griefer_utils.v1_8_9.features.modules" + getControlName();
	}

	/*
	TODO:
	@Override
	public int getLines() {
		return LeaderboardHandler.countLines();
	}

	@Override
	public List<List<Text>> getTexts() {
		List<List<Text>> texts = super.getTexts();
		if (LeaderboardHandler.data != null && leaderboard.get() == LeaderboardDisplayType.COMPACT)
			texts.get(0).add(toText(LeaderboardHandler.getCompactText()));

		return texts;
	}

	@Override
	public double getRawWidth() {
		return Math.max(super.getRawWidth(), LeaderboardHandler.renderWidth);
	}

	Text toText(String text) {
		return new Text(text, valueColor, bold, italic, underline);
	}
*/
	@Override
	public String[] getValues() {
		return getDefaultValues();
	}

	@Override
	public String[] getDefaultValues() {
		StringBuilder value = new StringBuilder();

		if (displayType.get() != RoundDisplayType.FLOWN)
			value.append("  ").append(DECIMAL_FORMAT_98.format(RoundHandler.roundsRan));
		if (displayType.get() == RoundDisplayType.BOTH)
			value.append(" ");
		if (displayType.get() != RoundDisplayType.RAN)
			value.append("  ").append(DECIMAL_FORMAT_98.format(RoundHandler.roundsFlown));

		return new String[] { value.toString() };
	}

	/*
	TODO:
	@Override
	public void draw(double x, double y, double rightX) {
		super.draw(x, y, rightX);

		double xDiff = getStringWidth(getKeys()[0]);

		// Add padding
		y += padding;
		if (rightX == -1)
			xDiff += padding;

		if (isKeyVisible()) {
			switch (getDisplayFormatting()) {
				case SQUARE_BRACKETS:
					xDiff += getStringWidth("[]");
					break;
				case BRACKETS:
					xDiff += getStringWidth(">");
					break;
				case COLON:
					xDiff += getStringWidth(":");
					break;
				case HYPHEN:
					xDiff += getStringWidth(" -");
					break;
			}

			xDiff += getStringWidth(" ");
		}

		if (displayType.get() != RoundDisplayType.FLOWN) {
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/speed.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(x + xDiff, y, 256, 256, 7, 7);
			xDiff += getStringWidth("  " + RoundHandler.roundsRan);
		}

		if (displayType.get() == RoundDisplayType.BOTH)
			xDiff += getStringWidth(" ");

		if (displayType.get() != RoundDisplayType.RAN) {
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/fly.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(x + xDiff, y, 256, 256, 7, 7);
		}

		if (leaderboard.get() == LeaderboardDisplayType.ON && LeaderboardHandler.data != null && player() != null)
			LeaderboardHandler.draw(x + padding, y);
	}
	int getStringWidth(String text) {
		return mc.fontRendererObj.getStringWidth(toText(text).getText());
	}
*/

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		RoundHandler.checkForRounds(notificationType.get().notifier);
	}

	private enum NotificationType implements Named {

		NONE("Keine", s -> {}),
		TOAST("Erfolg", s -> LabyBridge.labyBridge.notify("§aSpawn-Runden Zähler", s)),
		ACTIONBAR("Aktionsleiste", s -> mc().ingameGUI.setRecordPlaying(s, true)),
		MESSAGE("Chatnachricht", s -> display(Constants.ADDON_PREFIX + s));

		private final String name;
		private final Consumer<String> notifier;

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
