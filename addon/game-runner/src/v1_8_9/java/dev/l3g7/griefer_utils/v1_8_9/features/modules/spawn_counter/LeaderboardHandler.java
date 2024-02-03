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
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import dev.l3g7.griefer_utils.v1_8_9.settings.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.name;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.uuid;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.*;

class LeaderboardHandler {

	private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();
	public LeaderboardData data;

	private final SpawnCounter spawnCounter;
	private TextLine previousRank, ownRank, nextRank;

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
		nextRank = spawnCounter.createRawLine();
		ownRank = spawnCounter.createRawLine();
		previousRank = spawnCounter.createRawLine();
	}

	public void tickLines() {
		LeaderboardDisplayType displayType = spawnCounter.leaderboard.get();

		if (displayType == LeaderboardDisplayType.ON && data != null) {
			tickOtherPlayerLine(data.previous, previousRank, -1);
			tickOtherPlayerLine(data.next, nextRank, +1);

			ownRank.setState(VISIBLE);
			ownRank.updateAndFlush(
					Component.text(DECIMAL_FORMAT_98.format(data.position) + ". ")
							.append(Component.icon(Icon.head(uuid()), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT)) // TODO fix icon rendering?
							.append(Component.text(" " + name() + ": " + data.score))
			);
		} else {
			previousRank.setState(DISABLED);  // FIXME widget editor fallback?
			ownRank.setState(DISABLED);
			nextRank.setState(DISABLED);
		}
	}

	private void tickOtherPlayerLine(UserData other, TextLine line, int offset) {
		line.setState(DISABLED);

		if (other == null)
			return;

		PlayerListEntry entry = ENTRIES.get(other.uuid);
		if (entry == null || !entry.loaded)
			return;

		line.setState(VISIBLE);
		line.updateAndFlush(
			Component.text(DECIMAL_FORMAT_98.format(data.position + offset) + ". ", TextColor.color(0xAAAAAA))
				.append(Component.icon(Icon.head(UUID.fromString(entry.id)), Style.builder().color(TextColor.color(-1)).build(), MinecraftUtil.mc().fontRendererObj.FONT_HEIGHT))
				.append(Component.text(" " + entry.name + ": " + other.score, TextColor.color(0xAAAAAA)))
		);
	}

}
