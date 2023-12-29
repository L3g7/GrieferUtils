/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.modules.spawn_counter;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.misc.server.GUClient;
import dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListEntry;
import dev.l3g7.griefer_utils.util.MinecraftUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.COMPACT;
import static dev.l3g7.griefer_utils.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static dev.l3g7.griefer_utils.features.modules.spawn_counter.SpawnCounter.leaderboard;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

class LeaderboardHandler {

	private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();
	public static LeaderboardData data;

	@EventListener
	static void onGrieferGamesJoin(GrieferGamesJoinEvent event) {
		request(() -> GUClient.get().getLeaderboardData());
	}

	static void onRoundComplete(boolean flown) {
		request(() -> GUClient.get().sendLeaderboardData(flown));
	}

	private static void request(Supplier<LeaderboardData> supplier) {
		if (!GUClient.get().isAvailable() || leaderboard.get() == OFF)
			return;

		new Thread(() -> {
			data = supplier.get();

			if (data.next != null && !ENTRIES.containsKey(data.next.uuid))
				ENTRIES.put(data.next.uuid, new PlayerListEntry(null, data.next.uuid));

			if (data.previous != null && !ENTRIES.containsKey(data.previous.uuid))
				ENTRIES.put(data.previous.uuid, new PlayerListEntry(null, data.previous.uuid));
		}).start();
	}

 	public static List<String> getTexts(SpawnCounter spawnCounter) {
		if (leaderboard.get() == COMPACT) {
			return ImmutableList.of(" \u2503 " + data.position + ".: " + data.score);
		}

		List<String> lines = new ArrayList<>();

		if (data.next != null && ENTRIES.get(data.next.uuid).loaded)
			lines.add(String.format("§7%d.   %s: %d", data.position - 1, ENTRIES.get(data.next.uuid).name, data.next.score));

		lines.add(String.format("§f%d.   %s§r%s%d", data.position, MinecraftUtil.name(), spawnCounter.toText(": ", 0).getText(), data.score));

		if (data.previous != null && ENTRIES.get(data.previous.uuid).loaded)
			lines.add(String.format("§7%d.   %s: %d", data.position + 1, ENTRIES.get(data.previous.uuid).name, data.previous.score));

		return lines;
	}

	public static void draw(SpawnCounter sc, double x, double y, double rightX) {
		if (rightX != -1) {
			drawRight(sc, x, y, rightX);
			return;
		}

		x += sc.isBold() ? 3 : 2;

		if (data.next != null && ENTRIES.get(data.next.uuid).loaded)
			ENTRIES.get(data.next.uuid).renderSkull(x + sc.getStringWidth(data.position - 1 + "."), y += 10, 8);

		y += 10;
		mc().getTextureManager().bindTexture(player().getLocationSkin());
		double playerX = x + sc.getStringWidth(data.position + ".");
		drawUtils().drawTexture(playerX, y, 32, 32, 32, 32, 8, 8); // First layer
		drawUtils().drawTexture(playerX, y, 160, 32, 32, 32, 8, 8); // Second layer

		if (data.previous != null && ENTRIES.get(data.previous.uuid).loaded)
			ENTRIES.get(data.previous.uuid).renderSkull(x + sc.getStringWidth(data.position + 1 + "."), y + 10, 8);
	}

	private static void drawRight(SpawnCounter sc, double x, double y, double rightX) {
		x = rightX + (sc.isBold() ? 3 : 2);

		List<String> texts = getTexts(sc);

		if (data.next != null && ENTRIES.get(data.next.uuid).loaded)
			ENTRIES.get(data.next.uuid).renderSkull(x + sc.getStringWidth(data.position - 1 + ".") - sc.getStringWidth(texts.get(0)), y += 10, 8);

		y += 10;
		mc().getTextureManager().bindTexture(player().getLocationSkin());
		int yHeight = player().getSkinType().equals("slim") ? 64 : 32; // Old textures are 32x64
		double playerX = x + sc.getStringWidth(data.position + ".") - sc.getStringWidth(texts.get(data.next == null ? 0 : 1));
		drawUtils().drawTexture(playerX, y, 32, yHeight, 32, yHeight, 8, 8); // First layer
		drawUtils().drawTexture(playerX, y, 160, yHeight, 32, yHeight, 8, 8); // Second layer

		if (data.previous != null && ENTRIES.get(data.previous.uuid).loaded)
			ENTRIES.get(data.previous.uuid).renderSkull(x + sc.getStringWidth(data.position + 1 + ".") - sc.getStringWidth(texts.get(texts.size() - 1)), y + 10, 8);
	}

}
