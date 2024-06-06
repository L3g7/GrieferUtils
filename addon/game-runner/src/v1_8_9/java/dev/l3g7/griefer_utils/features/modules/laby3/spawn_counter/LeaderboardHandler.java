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

import com.google.common.base.Supplier;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry;
import net.labymod.main.ModTextures;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.features.modules.laby3.spawn_counter.SpawnCounter.LeaderboardDisplayType.OFF;
import static dev.l3g7.griefer_utils.features.modules.laby3.spawn_counter.SpawnCounter.LeaderboardDisplayType.ON;
import static dev.l3g7.griefer_utils.features.modules.laby3.spawn_counter.SpawnCounter.leaderboard;
import static dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry.INVALID_PLAYER;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.labymod.ingamegui.Module.mc;

@ExclusiveTo(LABY_3)
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
			renderSkull(entry, x, y, 8);
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

	private static void renderSkull(PlayerListEntry e, double x, double y, int size) {
		if (e.skin == null) {
			mc.getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		GlStateManager.bindTexture(e.skin.getGlTextureId());

		if (!e.isMojang()) {
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		int yHeight = e.oldSkin ? 64 : 32; // Old textures are 32x64
		DrawUtils.drawTexture(x, y, 32, yHeight, 32, yHeight, size, size); // First layer
		DrawUtils.drawTexture(x, y, 160, yHeight, 32, yHeight, size, size); // Second layer
	}

}
