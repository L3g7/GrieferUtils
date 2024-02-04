/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter;

import com.google.common.base.Supplier;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest.UserData;
import dev.l3g7.griefer_utils.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import dev.l3g7.griefer_utils.v1_8_9.settings.player_list.PlayerListEntry;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.DISABLED;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;

class LeaderboardHandler {

	private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();
	public LeaderboardData data;

	private final SpawnCounter spawnCounter;
	private LeaderboardLine previousRank, ownRank, nextRank;
	private float maxPosWidth;

	public LeaderboardHandler(SpawnCounter spawnCounter) {
		this.spawnCounter = spawnCounter;
		EventRegisterer.register(this);
	}

	@EventListener
	public void onGrieferGamesJoin(GrieferGamesJoinEvent event) {
		request(() -> GUClient.get().getLeaderboardData());
	}

	public void onRoundComplete(boolean flown) {
		request(() -> GUClient.get().sendLeaderboardData(flown));
	}

	private void request(Supplier<LeaderboardData> request) {
		if (!GUClient.get().isAvailable() || spawnCounter.leaderboard.get() == OFF)
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
		nextRank = new LeaderboardLine(false, +1);
		ownRank = new LeaderboardLine(true, 0);
		previousRank = new LeaderboardLine(false, -1);
	}

	public void tickLines() {
		LeaderboardDisplayType displayType = spawnCounter.leaderboard.get();

		if (displayType == LeaderboardDisplayType.ON && data != null) {
			maxPosWidth = 0;

			tickOtherPlayerLine(data.previous, previousRank);
			tickOtherPlayerLine(data.next, nextRank);

			ownRank.setState(VISIBLE);
			ownRank.updateLeaderboardLine(UUID.fromString("88c0f579-0b37-4c12-81c0-84daa2801023"), "L3g7", data.score);
		} else {
			previousRank.setState(DISABLED);  // FIXME widget editor fallback?
			ownRank.setState(DISABLED);
			nextRank.setState(DISABLED);
		}
	}

	private void tickOtherPlayerLine(UserData other, LeaderboardLine line) {
		line.setState(DISABLED);

		if (other == null)
			return;

		PlayerListEntry entry = ENTRIES.get(other.uuid);
		if (entry == null || !entry.loaded)
			return;

		line.setState(VISIBLE);
		line.updateLeaderboardLine(UUID.fromString(entry.id), entry.name, other.score);
	}

	public class LeaderboardLine extends TextLine {

		private final TextColor textColor;
		private final int offset;
		RenderableComponent first, second;

		public LeaderboardLine(boolean primary, int offset) {
			super(spawnCounter, (Component) null, "");
			this.textColor = TextColor.color(primary ? -1 : 0xAAAAAA);
			this.offset = offset;

			spawnCounter.addLine(this);
		}

		@Override
		protected void flushInternal() {}

		private RenderableComponent createRenderableComponent(Component c) {
			return RenderableComponent.builder().disableCache().format(c);
		}

		public void updateLeaderboardLine(UUID uuid, String name, int score) {
			setState(VISIBLE);

			this.first = createRenderableComponent(
				Component.text(DECIMAL_FORMAT_98.format(data.position + offset) + ". ", textColor));
			this.second = createRenderableComponent(
				Component.icon(new OffsetIcon(Icon.head(uuid), 0, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)
					.append(Component.text(" " + name + ": " + score, textColor)));
			renderableComponent = this.first;

			maxPosWidth = Math.max(maxPosWidth, this.first.getWidth());
		}

		@Override
		public void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize) {
			BUILDER.pos(x, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(first).render(stack);
			BUILDER.pos(x + maxPosWidth, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(second).render(stack);
		}

	}

}
