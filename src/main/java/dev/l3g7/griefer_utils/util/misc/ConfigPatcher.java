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

package dev.l3g7.griefer_utils.util.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.ArrayUtil;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class ConfigPatcher {

	JsonObject config;

	public ConfigPatcher(JsonObject config) {
		this.config = config;
	}

	public void patch() {
		if (!config.has("version"))
			return;

		String version = config.get("version").getAsString();
		if (version.startsWith("v1"))
			patch_v1();
	}

	private void patch_v1() {
		rename("features.chat_menu", "chat.chat_menu");
		rename("features.anti_command_choker.active", "chat.anti_command_choker.enabled");
		rename("features.armor_break_warning.active", "player.armor_break_warning.enabled");
		rename("features.auto_eat.trigger_mode", "player.auto_eat.trigger_mode");
		rename("features.auto_eat.preferred_food", "player.auto_eat.preferred_food");
		rename("features.auto_eat.active", "player.auto_eat.enabled");
		rename("features.auto_portal.maximize", "world.auto_portal.maximize");
		rename("features.auto_portal.join", "world.auto_portal.join");
		rename("features.auto_portal.active", "world.auto_portal.enabled");
		rename("features.calculator.auto_withdraw", "chat.calculator.auto_withdraw");
		rename("features.calculator.deposit_all", "chat.calculator.deposit_all");
		rename("features.calculator.placeholder", "chat.calculator.placeholder");
		rename("features.calculator.decimal_places", "chat.calculator.decimal_places");
		rename("features.calculator.active", "chat.calculator.enabled");
		rename("features.chat_time.style", "chat.chat_time.style");
		rename("features.chat_time.format", "chat.chat_time.format");
		rename("features.chat_time.active", "chat.chat_time.enabled");
		rename("features.chat_menu.active", "chat.chat_menu.enabled");
		rename("features.chat_menu.entries", "chat.chat_menu.entires");
		rename("features.chat_menu.entries.open_profile", "chat.chat_menu.entires.Profil öffnen");
		rename("features.chat_menu.entries.name_history", "chat.chat_menu.entires.Namensverlauf");
		rename("features.chat_menu.entries.copy_name", "chat.chat_menu.entires.Namen kopieren");
		rename("features.chat_menu.entries.search_forum", "chat.chat_menu.entires.Im Forum suchen");
		rename("features.chat_menu.entries.open_inv", "chat.chat_menu.entires.Inventar öffnen");
		rename("features.chat_menu.entries.view_gear", "chat.chat_menu.entires.Ausrüstung ansehen");
		rename("features.chat_menu.entries.open_ec", "chat.chat_menu.entires.EC öffnen");
		patchObjectArray("features.chat_menu.entries.custom", "chat.chat_menu.entries.custom",
			"action", _enum("action", "open_url", "OPEN_URL", "run", "RUN_CMD", "suggest", "SUGGEST_CMD"),
			"value", "command",
			"icon_type", _enum("icon_type", "default", "DEFAULT", "item", "ITEM", "image", "IMAGE_FILE"),
			"icon", chatMenuIconPatch,
			"enabled", null
		);
		rename("features.chat_reactor.active", "chat.chat_reactor.enabled");
		patchObjectArray("features.chat_reactor.entries", "chat.chat_reactor.entries",
			"is_regex", "is_regex",
			"compare_everything", "match_all",
			"trigger", "trigger",
			"command", "command",
			"name", null);
		rename("features.chest_search.active", "world.chest_search.enabled");
		rename("features.chunk_indicator.red", "world.chunk_indicator.red_lines");
		rename("features.chunk_indicator.yellow", "world.chunk_indicator.yellow_lines");
		rename("features.chunk_indicator.cyan", "world.chunk_indicator.cyan_lines");
		rename("features.chunk_indicator.blue", "world.chunk_indicator.blue_lines");
		rename("features.chunk_indicator.toggle", "world.chunk_indicator.key");
		rename("features.cooldown_notifications.active", "player.cooldown_notifications.enabled");
		rename("features.cooldown_notifications.end_dates", "player.cooldown_notifications.end_dates");
		rename("features.item_saver.bonze_saver.active", "item.sword_saver.enabled");
		rename("features.item_saver.border_saver.active", "item.border_saver.enabled");
		rename("features.item_saver.prefix_saver.active", "item.prefix_saver.enabled");
		rename("features.tool_saver.save_non_repairable", "item.tool_saver.save_non_repairable");
		rename("features.tool_saver.damage", "item.tool_saver.damage");
		rename("features.npc_ghost_hand.active", "world.n_p_c_entity_ghost_hand.enabled");
		rename("features.npc_ghost_hand.active", "world.n_p_c_entity_ghost_hand.enabled");
		rename("features.scammer_list.tab", "player.scammer_list.tab_action");
		rename("features.scammer_list.chat", "player.scammer_list.chat_action");
		rename("features.scammer_list.display_name", "player.scammer_list.display_name_action");
		rename("features.scammer_list.show_in_profile", "player.scammer_list.show_in_profile");
		rename("features.scammer_list.active", "player.scammer_list.enabled");
		rename("features.trusted_list.tab", "player.trusted_list.tab_action");
		rename("features.trusted_list.chat", "player.trusted_list.chat_action");
		rename("features.trusted_list.display_name", "player.trusted_list.display_name_action");
		rename("features.trusted_list.show_in_profile", "player.trusted_list.show_in_profile");
		rename("features.trusted_list.active", "player.trusted_list.enabled");
		rename("features.real_money.tag", "chat.real_money.tag");
		rename("features.real_money.position", "chat.real_money.position");
		rename("features.real_money.active", "chat.real_money.enabled");
		rename("features.self_disguise.active", "world.self_disguise.enabled");
		rename("features.show_joins.active", "world.show_joins.enabled");
		rename("features.show_joins.filter.active", "world.show_joins.filter");
		rename("features.show_joins.filter.active", "world.show_joins.filter");
		rename("features.trajectories.mode", "render.trajectories.mode");
		rename("misc.auto_update.show_screen", "settings.auto_update.show_changelog");
		rename("misc.auto_update.active", "settings.auto_update.enabled");
		rename("modules.booster.key_mode", "modules.booster.design");
		rename("modules.clear_lag.shorten", "modules.clear_lag.time_format", moduleTimerFormatPatch);
		rename("modules.mob_remover.shorten", "modules.mob_remover.time_format", moduleTimerFormatPatch);
		rename("tweaks.auto_sprint.active", "player.auto_sprint.enabled");
		rename("tweaks.auto_unnick.tab", "chat.auto_unnick.tab");
		rename("tweaks.auto_unnick.active", "chat.auto_unnick.enabled");
		rename("tweaks.better_switch_cmd.active", "chat.better_switch_command.enabled");
		rename("tweaks.better_sign.active", "world.better_sign.enabled");
		rename("tweaks.book_reader.active", "item.book_fix.enabled");
		rename("tweaks.chat_mods.anti_clear_chat.active", "chat.chat_mods.anti_clear_chat");
		rename("tweaks.chat_mods.remove_supreme_spaces.active", "chat.chat_mods.remove_supreme_spaces");
		rename("tweaks.chat_mods.news.mode", "chat.chat_mods.news");
		rename("tweaks.chat_mods.remove_streamer.active", "chat.chat_mods.remove_streamer_notifications");
		rename("tweaks.chat_mods.stfu_mysterymod.active", "chat.chat_mods.stfu_mystery_mod");
		rename("tweaks.clan_tags.active", "render.clan_tags.enabled");
		rename("tweaks.command_logger.active", "chat.command_logger.enabled");
		rename("tweaks.full_bright.active", "render.full_bright.enabled");
		rename("tweaks.item_info.item_counter.ignore_sub_ids", "item.item_info.item_counter.ignore_damage");
		rename("tweaks.item_info.item_counter.active", "item.item_info.item_counter.enabled");
		rename("tweaks.item_info.repair_value_viewer.active", "item.item_info.repair_value_viewer.enabled");
		rename("tweaks.name_tag_prefix_sync.active", "render.name_tag_prefix_sync.enabled");
		rename("tweaks.no_fire_overlay.active", "render.no_fire_overlay.enabled");
		rename("tweaks.no_fog.blindness", "render.no_fog.blindness");
		rename("tweaks.no_fog.water", "render.no_fog.water");
		rename("tweaks.no_fog.lava", "render.no_fog.lava");
		rename("tweaks.no_fog.active", "render.no_fog.enabled");
		rename("tweaks.player_hider.active", "render.player_hider.enabled");
		rename("tweaks.plot_chat_indicator.states", "chat.plot_chat_indicator.states");
		rename("tweaks.portal_cooldown.active", "world.portal_cooldown.enabled");
		rename("tweaks.bank_scoreboard.active", "player.bank_scoreboard.enabled");
		rename("tweaks.orb_balance.active", "player.orb_scoreboard.enabled");
		rename("tweaks.showbarriers.active", "render.show_barriers.enabled");
		rename("tweaks.enlighten.light_gray", "chat.enlighten.enlighten_light_gray");
		rename("tweaks.enlighten.gray", "chat.enlighten.enlighten_gray");
		rename("tweaks.enlighten.black", "chat.enlighten.enlighten_black");
		rename("tweaks.enlighten.active", "chat.enlighten.enabled");
		rename("tweaks.enlighten.chat", "chat.enlighten.chat");
		rename("tweaks.enlighten.tab", "chat.enlighten.tab");
		rename("tweaks.enlighten.item", "chat.enlighten.item");
		rename("tweaks.no_magic_text.active", "chat.no_magic_text.enabled");
		rename("tweaks.no_magic_text.chat", "chat.no_magic_text.chat");
		rename("tweaks.no_magic_text.tab", "chat.no_magic_text.tab");
		rename("tweaks.no_magic_text.item", "chat.no_magic_text.item");
		rename("tweaks.true_sight.opacity", "render.true_sight.opacity");
		rename("tweaks.true_sight.active", "render.true_sight.enabled");
		rename("tweaks.webhooks.active", "chat.filter_webhooks.enabled");
		rename("tweaks.webhooks.filter", "chat.filter_webhooks.filter");
	}

	private void rename(String previousKey, String newKey, Patcher... patchers) {
		JsonObject pParent = getParent(previousKey);
		JsonObject nParent = getParent(newKey);
		for (Patcher patcher : patchers)
			call(patcher, pParent, nParent, pParent.get(getKey(previousKey)));

		if (patchers.length == 0 && pParent.get(getKey(newKey)) != null)
			nParent.add(getKey(newKey), pParent.get(getKey(newKey)));
	}

	private void patchObjectArray(String previousKey, String newKey, Object... changes) {
		JsonObject pParent = getParent(previousKey);
		JsonObject nParent = getParent(newKey);
		if (pParent.has(getKey(previousKey))) {
			JsonArray nA = new JsonArray();
			for (JsonElement e : pParent.get(getKey(previousKey)).getAsJsonArray()) {
				JsonObject old = e.getAsJsonObject();
				JsonObject nw = new JsonObject();
				for (int i = 0; i < changes.length; i += 2)
					call(changes[i + 1], pParent, nw, old.get((String) changes[0]));
				nA.add(nw);
			}
			nParent.add(getKey(newKey), nA);
		}
	}

	private Patcher _enum(String key, String... changes) {
		return (parent, newParent, element) -> {
			for (int i = 0; i < changes.length; i += 2) {
				if (changes[i].equalsIgnoreCase(element.getAsString())) {
					newParent.addProperty(key, changes[i + 1]);
					return;
				}
			}
		};
	}

	private void call(Object patch, JsonObject oldParent, JsonObject newParent, JsonElement element) {
		if (patch instanceof Patcher)
			((Patcher) patch).patch(oldParent, newParent, element);
		else if (patch instanceof String)
			newParent.add((String) patch, element);
	}

	private final Patcher moduleTimerFormatPatch = (parent, newParent, element) -> {
		if (element.getAsBoolean())
			newParent.addProperty("time_format", "SHORT");
		else
			newParent.addProperty("time_format", "LONG");
	};

	private final Patcher chatMenuIconPatch = (parent, newParent, element) -> {
		String newValue = null;
		switch (parent.get("icon_type").getAsString()) {
			case "item":
				newValue = element.getAsString();
				break;
			case "image":
				try {
					newValue = Base64.getEncoder().encodeToString(IOUtils.toByteArray(new FileInputStream(element.getAsString())));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		newParent.addProperty("icon", newValue);
	};

	private interface Patcher {
		void patch(JsonObject oldParent, JsonObject newParent, JsonElement element);
	}

	private JsonObject getParent(String path) {
		String[] parts = path.split("\\.");
		for (int i = 0; i < parts.length - 1; i++) {
			if (!config.has(parts[i]) || !(config.get(parts[i]).isJsonObject()))
				config.add(parts[i], new JsonObject());
			config = config.get(parts[i]).getAsJsonObject();
		}
		return config;
	}

	private String getKey(String path) {
		return ArrayUtil.last(path.split("\\."));
	}
}