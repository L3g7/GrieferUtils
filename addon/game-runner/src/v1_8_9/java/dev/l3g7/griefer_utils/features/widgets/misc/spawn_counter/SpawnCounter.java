/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.misc.spawn_counter;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.LabyWidget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry.INVALID_PLAYER;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.features.widgets.misc.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.DISABLED;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;

@Singleton
public class SpawnCounter extends LabyWidget { // NOTE: cleanup

	final RoundHandler roundHandler = new RoundHandler(this);
	LeaderboardHandler leaderboardHandler;

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
				onGrieferGamesJoin(null);
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

	@EventListener
	public void onGrieferGamesJoin(ServerEvent.GrieferGamesJoinEvent event) {
		leaderboardHandler.request(() -> GUClient.get().getLeaderboardData());
	}

	public void onRoundComplete(boolean flown) {
		leaderboardHandler.request(() -> GUClient.get().sendLeaderboardData(flown));
	}

	@Override
	protected Object getLaby3() {
		return new SpawnCounterL3();
	}

	@Override
	protected Object getLaby4() {
		return new SpawnCounterL4();
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

	static abstract class LeaderboardHandler {
		public LeaderboardRequest.LeaderboardData data;

		public abstract void request(Supplier<LeaderboardRequest.LeaderboardData> request);
	}

	@ExclusiveTo(LABY_3)
	public class SpawnCounterL3 extends Laby3Widget {

		public SpawnCounterL3() {
			leaderboardHandler = new LeaderboardHandlerL3();
		}

		@Override
		public String getComparisonName() {
			return "dev.l3g7.griefer_utils.features.widgets" + getControlName();
		}

		@Override
		public int getLines() {
			return ((LeaderboardHandlerL3) leaderboardHandler).countLines();
		}

		@Override
		public List<List<Text>> getTexts() {
			List<List<Text>> texts = super.getTexts();
			if (LeaderboardHandlerL3.data != null && leaderboard.get() == LeaderboardDisplayType.COMPACT)
				texts.get(0).add(toText(LeaderboardHandlerL3.getCompactText()));

			return texts;
		}

		@Override
		public double getRawWidth() {
			return Math.max(super.getRawWidth(), LeaderboardHandlerL3.renderWidth);
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
				value.append("  ").append(DECIMAL_FORMAT_98.format(roundHandler.roundsRan));
			if (displayType.get() == RoundDisplayType.BOTH)
				value.append(" ");
			if (displayType.get() != RoundDisplayType.RAN)
				value.append("  ").append(DECIMAL_FORMAT_98.format(roundHandler.roundsFlown));

			return new String[]{value.toString()};
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
				xDiff += getStringWidth("  " + roundHandler.roundsRan);
			}

			if (displayType.get() == RoundDisplayType.BOTH)
				xDiff += getStringWidth(" ");

			if (displayType.get() != RoundDisplayType.RAN) {
				mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/fly.png"));
				LabyMod.getInstance().getDrawUtils().drawTexture(x + xDiff, y, 256, 256, 7, 7);
			}

			if (leaderboard.get() == LeaderboardDisplayType.ON && LeaderboardHandlerL3.data != null && player() != null)
				LeaderboardHandlerL3.draw(x + padding, y);
		}

		int getStringWidth(String text) {
			return mc.fontRendererObj.getStringWidth(toText(text).getText());
		}

		@ExclusiveTo(LABY_3)
		class LeaderboardHandlerL3 extends LeaderboardHandler {

			private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();
			public static LeaderboardData data;
			public static SpawnCounterL3 sc;
			public static int renderWidth;

			@Override
			public void request(Supplier<LeaderboardData> supplier) {
				if (!GUClient.get().isAvailable() || leaderboard.get() == OFF)
					return;

				new Thread(() -> {
					data = supplier.get();
					if (data == null)
						return;

					if (data.next != null && !ENTRIES.containsKey(data.next.uuid))
						ENTRIES.put(data.next.uuid, new PlayerListEntry(null, data.next.uuid));

					if (data.previous != null && !ENTRIES.containsKey(data.previous.uuid))
						ENTRIES.put(data.previous.uuid, new PlayerListEntry(null, data.previous.uuid));
				}).start();
			}

			public static String getCompactText() {
				return " ┃ " + data.position + ".: " + data.score;
			}

			public int countLines() {
				if (data == null || leaderboard.get() != LeaderboardDisplayType.ON)
					return 1;

				int i = 2; // Yourself + the normal line

				if (getEntry(data.next).loaded)
					i++;
				if (getEntry(data.previous).loaded)
					i++;

				return i;
			}

			public static void draw(double x, double y) {
				List<Triple<Integer, PlayerListEntry, Integer>> renderData = getRenderData();

				int maxPosWidth = 0;
				for (Triple<Integer, PlayerListEntry, Integer> datum : renderData) {
					String text = sc.toText(DECIMAL_FORMAT_98.format(datum.getLeft()) + ".").getText();
					DrawUtils.drawStringWithShadow(text, x, y += 10, datum.getMiddle() == null ? -1 : 0xAAAAAA);

					maxPosWidth = Math.max(DrawUtils.getStringWidth(text), maxPosWidth);
				}

				x += maxPosWidth + 2;
				y -= 10 * renderData.size();

				int maxNameWidth = 0;
				for (Triple<Integer, PlayerListEntry, Integer> datum : renderData)
					maxNameWidth = Math.max(maxNameWidth, drawEntry(datum.getRight(), datum.getMiddle(), x, y += 10));

				renderWidth = maxPosWidth + maxNameWidth + 13;
			}

			private static int drawEntry(int score, PlayerListEntry entry, double x, double y) {
				// Render skull
				if (entry != null) {
					renderSkull(entry, x, y);
				} else {
					mc.getTextureManager().bindTexture(player().getLocationSkin());
					DrawUtils.drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
					DrawUtils.drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer
				}

				String text = sc.toText((entry == null ? mc.getSession().getUsername() : entry.name) + ": " + DECIMAL_FORMAT_98.format(score)).getText();
				DrawUtils.drawStringWithShadow(text, x + 11, y, entry == null ? -1 : 0xAAAAAA);
				return DrawUtils.getStringWidth(text);
			}

			private static List<Triple<Integer, PlayerListEntry, Integer>> getRenderData() {
				List<Triple<Integer, PlayerListEntry, Integer>> renderData = new ArrayList<>();
				if (getEntry(data.next).loaded)
					renderData.add(Triple.of(data.position - 1, getEntry(data.next), data.next.score));

				renderData.add(Triple.of(data.position, null, data.score));

				if (getEntry(data.previous).loaded)
					renderData.add(Triple.of(data.position + 1, getEntry(data.previous), data.previous.score));

				return renderData;
			}

			private static PlayerListEntry getEntry(LeaderboardRequest.UserData data) {
				if (data == null)
					return INVALID_PLAYER;

				PlayerListEntry entry = ENTRIES.get(data.uuid);
				if (entry != null)
					return entry;

				return INVALID_PLAYER;
			}

			private static void renderSkull(PlayerListEntry e, double x, double y) {
				if (e.skin == null) {
					mc.getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
					DrawUtils.drawTexture(x, y, 0, 0, 256, 256, 8, 8);
					return;
				}

				GlStateManager.bindTexture(e.skin.getGlTextureId());

				if (!e.isMojang()) {
					DrawUtils.drawTexture(x, y, 0, 0, 256, 256, 8, 8);
					return;
				}

				int yHeight = e.oldSkin ? 64 : 32; // Old textures are 32x64
				DrawUtils.drawTexture(x, y, 32, yHeight, 32, yHeight, 8, 8); // First layer
				DrawUtils.drawTexture(x, y, 160, yHeight, 32, yHeight, 8, 8); // Second layer
			}

		}

	}

	@ExclusiveTo(LABY_4)
	public class SpawnCounterL4 extends Laby4Widget {

		public SpawnCounterL4() {
			leaderboardHandler = new LeaderboardHandlerL4();
		}

		@Override
		protected void createText() {
			super.createText();
			((LeaderboardHandlerL4) leaderboardHandler).createLines();
		}

		@Override
		public void onTick(boolean isEditorContext) {
			super.onTick(isEditorContext);
			((LeaderboardHandlerL4) leaderboardHandler).tickLines();
		}

		@Override
		public Object getValue() {
			Component value = Component.empty();

			if (displayType.get() != RoundDisplayType.FLOWN)
				value.append(Component.icon(Icons.of("speed", -2, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT))
					.append(Component.text(roundHandler.roundsRan));

			if (displayType.get() == RoundDisplayType.BOTH)
				value.append(Component.text(" "));

			if (displayType.get() != RoundDisplayType.RAN)
				value.append(Component.icon(Icons.of("booster/fly", -2, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)) // NOTE: cleanup
					.append(Component.text(roundHandler.roundsFlown));

			if (leaderboard.get() == LeaderboardDisplayType.COMPACT)
				value.append(Component.text(" ┃ " + leaderboardHandler.data.position + ".: " + leaderboardHandler.data.score));

			return value;
		}

		@Override
		public String getComparisonName() {
			return "dev.l3g7.griefer_utils.features.widgets" + enabled.name();
		}

		@ExclusiveTo(LABY_4)
		class LeaderboardHandlerL4 extends LeaderboardHandler {

			private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();

			private LeaderboardLine previousRank, ownRank, nextRank;
			private float maxPosWidth;

			public LeaderboardHandlerL4() {
				EventRegisterer.register(this);
			}

			public void request(Supplier<LeaderboardRequest.LeaderboardData> request) {
				if (!GUClient.get().isAvailable() || leaderboard.get() == OFF)
					return;

				new Thread(() -> {
					data = request.get();
					if (data == null)
						return;

					if (data.next != null && !ENTRIES.containsKey(data.next.uuid))
						ENTRIES.put(data.next.uuid, new PlayerListEntry(null, data.next.uuid));

					if (data.previous != null && !ENTRIES.containsKey(data.previous.uuid))
						ENTRIES.put(data.previous.uuid, new PlayerListEntry(null, data.previous.uuid));
				}).start();
			}

			public void createLines() {
				nextRank = new LeaderboardLine(false, -1);
				ownRank = new LeaderboardLine(true, 0);
				previousRank = new LeaderboardLine(false, +1);
			}

			public void tickLines() {
				LeaderboardDisplayType displayType = leaderboard.get();

				if (displayType == LeaderboardDisplayType.ON && data != null) {
					maxPosWidth = 0;

					tickOtherPlayerLine(data.previous, previousRank);
					tickOtherPlayerLine(data.next, nextRank);

					ownRank.setState(VISIBLE);
					ownRank.updateLeaderboardLine(uuid(), name(), data.score);
				} else {
					previousRank.setState(DISABLED);  // FIXME widget editor fallback?
					ownRank.setState(DISABLED);
					nextRank.setState(DISABLED);
				}
			}

			private void tickOtherPlayerLine(LeaderboardRequest.UserData other, LeaderboardLine line) {
				line.setState(DISABLED);

				if (other == null)
					return;

				PlayerListEntry entry = ENTRIES.get(other.uuid);
				if (entry == null || !entry.loaded)
					return;

				line.setState(VISIBLE);
				line.updateLeaderboardLine(UUID.fromString(entry.id), entry.name, other.score);
			}

			@ExclusiveTo(LABY_4)
			public class LeaderboardLine extends CustomRenderTextLine {

				private final TextColor textColor;
				private final int offset;
				RenderableComponent first, second;

				public LeaderboardLine(boolean primary, int offset) {
					super(SpawnCounterL4.this);
					this.textColor = TextColor.color(primary ? -1 : 0xAAAAAA);
					this.offset = offset;
					lines.add(this);
				}

				@Override
				public boolean isAvailable() {
					return first != null && second != null;
				}

				@Override
				public float getWidth() {
					return maxPosWidth + (second == null ? 0 : second.getWidth());
				}

				public void updateLeaderboardLine(UUID uuid, String name, int score) {
					setState(VISIBLE);

					this.first = createRenderableComponent(
						Component.text(DECIMAL_FORMAT_98.format(data.position + offset) + ". ", textColor));
					this.second = createRenderableComponent(
						Component.icon(Icons.of(Icon.head(uuid), 0, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)
							.append(Component.text(" " + name + ": " + score, textColor)));

					maxPosWidth = Math.max(maxPosWidth, this.first.getWidth());
				}

				@Override
				public void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize) {
					BUILDER.pos(x, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(first).render(stack);
					BUILDER.pos(x + maxPosWidth, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(second).render(stack);
				}

			}

		}

	}

}
