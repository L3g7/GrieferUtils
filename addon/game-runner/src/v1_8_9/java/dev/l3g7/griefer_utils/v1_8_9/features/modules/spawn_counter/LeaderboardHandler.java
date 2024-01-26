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

package dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter;

import com.google.common.base.Supplier;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest;
import dev.l3g7.griefer_utils.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import dev.l3g7.griefer_utils.v1_8_9.settings.player_list.PlayerListEntry;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.LeaderboardDisplayType.ON;
import static dev.l3g7.griefer_utils.v1_8_9.features.modules.spawn_counter.SpawnCounter.leaderboard;
import static dev.l3g7.griefer_utils.v1_8_9.settings.player_list.PlayerListEntry.INVALID_PLAYER;

class LeaderboardHandler {

	private static final Map<String, PlayerListEntry> ENTRIES = new ConcurrentHashMap<>();
	public static LeaderboardData data;
	public static SpawnCounter sc;
	public static int renderWidth;

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
			if (data == null)
				return;

			if (data.next != null && !ENTRIES.containsKey(data.next.uuid))
				ENTRIES.put(data.next.uuid, new PlayerListEntry(null, data.next.uuid));

			if (data.previous != null && !ENTRIES.containsKey(data.previous.uuid))
				ENTRIES.put(data.previous.uuid, new PlayerListEntry(null, data.previous.uuid));
		}).start();
	}

	public static String getCompactText() {
		return " \u2503 " + data.position + ".: " + data.score;
	}

	public static int countLines() {
		if (data == null || leaderboard.get() != ON)
			return 1;

		int i = 2; // Yourself + the normal line

		if (getEntry(data.next).loaded)
			i++;
		if (getEntry(data.previous).loaded)
			i++;

		return i;
	}
/* TODO:
	public static void draw(double x, double y) {
		List<Triple<Integer, PlayerListEntry, Integer>> renderData = getRenderData();

		int maxPosWidth = 0;
		for (Triple<Integer, PlayerListEntry, Integer> datum : renderData) {
			String text = sc.toText(DECIMAL_FORMAT_98.format(datum.getLeft()) + ".").getText();
			drawUtils().drawStringWithShadow(text, x, y += 10, datum.getMiddle() == null ? -1 : 0xAAAAAA);

			maxPosWidth = Math.max(drawUtils().getStringWidth(text), maxPosWidth);
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
			entry.renderSkull(x, y, 8);
		} else {
			mc().getTextureManager().bindTexture(player().getLocationSkin());
			drawUtils().drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
			drawUtils().drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer
		}

		String text = sc.toText((entry == null ? mc().getSession().getUsername() : entry.name) + ": " + DECIMAL_FORMAT_98.format(score)).getText();
		drawUtils().drawStringWithShadow(text, x + 11, y, entry == null ? -1 : 0xAAAAAA);
		return drawUtils().getStringWidth(text);
	}*/

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
		return entry == null ? INVALID_PLAYER : entry;
	}

}
