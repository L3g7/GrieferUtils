/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.features.modules.laby3.spawn_counter;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.labymod.main.LabyMod;
import net.labymod.utils.Material;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
@ExclusiveTo(LABY_3)
public class SpawnCounter extends Laby3Module {

	static final String configKey = "modules.spawn_counter.rounds_";

	private final DropDownSetting<NotificationType> notificationType = DropDownSetting.create(NotificationType.class)
		.name("Nachricht")
		.description("Wie die Benachrichtung aussehen soll, wenn eine Runde abgeschlossen wurde.")
		.icon(Material.WATCH)
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
		return "dev.l3g7.griefer_utils.features.modules" + getControlName();
	}

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

	public Text toText(String text) {
		return super.toText(text);
	}

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

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		RoundHandler.checkForRounds(notificationType.get().notifier);
	}

	private enum NotificationType implements Named {

		NONE("Keine", s -> {}),
		TOAST("Erfolg", s -> LabyBridge.labyBridge.notify("§aSpawn-Runden Zähler", s)),
		ACTIONBAR("Aktionsleiste", s -> mc.ingameGUI.setRecordPlaying(s, true)),
		MESSAGE("Chatnachricht", s -> display(ADDON_PREFIX + s));

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
