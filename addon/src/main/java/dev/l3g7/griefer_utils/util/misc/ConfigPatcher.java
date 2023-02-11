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
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.util.ArrayUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;

public class ConfigPatcher {

	JsonObject config;

	public ConfigPatcher(JsonObject config) {
		this.config = config;
	}

	public void patch() {
		if (!config.has("version"))
			return;

		String version = config.get("version").getAsString();
		if (version.startsWith("1"))
			patch_v1();

		VersionComparator cmp = new VersionComparator();

		// patch city_build added to reactions in 2.0-BETA-6
		if (cmp.compare("2.0-BETA-6", version) < 0) {
			JsonObject chatReactor = getParent("chat.chat_reactor.entries");
			if (chatReactor.has("entries")) {
				for (JsonElement entry : chatReactor.get("entries").getAsJsonArray()) {
					JsonObject reaction = entry.getAsJsonObject();
					if (!reaction.has("city_build"))
						reaction.addProperty("city_build", "Jeder CB");
				}
			}
		}
	}

	private void patch_v1() {
		rename("features.anti_command_choker.active", "chat.anti_command_choker.enabled");
		rename("features.armor_break_warning.active", "player.armor_break_warning.threshold");
		rename("features.auto_eat.trigger_mode", "player.auto_eat.trigger_mode");
		rename("features.auto_eat.preferred_food", "player.auto_eat.preferred_food");
		rename("features.auto_eat.active", "player.auto_eat.enabled");
		rename("features.auto_portal.maximize", "world.auto_portal.maximize");
		rename("features.auto_portal.join", "world.auto_portal.join");
		rename("features.auto_portal.active", "world.auto_portal.enabled");
		rename("features.calculator.auto_withdraw", "chat.calculator.auto_withdraw");
		rename("features.calculator.deposit_all", "chat.calculator.deposit_all");
		rename("features.calculator.placeholder", "chat.calculator.placeholder");
		rename("features.calculator.auto_equation_detect", "chat.calculator.auto_equation_detect");
		rename("features.calculator.decimal_places", "chat.calculator.decimal_places");
		rename("features.calculator.active", "chat.calculator.enabled");
		rename("features.chat_time.style", "chat.chat_time.style");
		rename("features.chat_time.format", "chat.chat_time.format");
		rename("features.chat_time.active", "chat.chat_time.enabled");
		rename("features.chat_menu.active", "chat.chat_menu.enabled");
		rename("features.chat_menu.entries.open_profile", "chat.chat_menu.entries.Profil öffnen");
		rename("features.chat_menu.entries.name_history", "chat.chat_menu.entries.Namensverlauf");
		rename("features.chat_menu.entries.copy_name", "chat.chat_menu.entries.Namen kopieren");
		rename("features.chat_menu.entries.search_forum", "chat.chat_menu.entries.Im Forum suchen");
		rename("features.chat_menu.entries.open_inv", "chat.chat_menu.entries.Inventar öffnen");
		rename("features.chat_menu.entries.view_gear", "chat.chat_menu.entries.Ausrüstung ansehen");
		rename("features.chat_menu.entries.open_ec", "chat.chat_menu.entries.EC öffnen");
		patchObjectArray("features.chat_menu.entries.custom", "chat.chat_menu.entries.custom",
			"action", "action", enumPatch("open_url", "OPEN_URL", "run", "RUN_CMD", "suggest", "SUGGEST_CMD"),
			"value", "command", chatMenuCommandPatch,
			"icon_type", "icon_type", enumPatch("default", "DEFAULT", "item", "ITEM", "image", "IMAGE_FILE"),
			"icon", "icon", chatMenuIconPatch,
			"enabled", "enabled",
			"name", "name"
		);
		rename("features.chat_reactor.active", "chat.chat_reactor.enabled");
		patchObjectArray("features.chat_reactor.entries", "chat.chat_reactor.entries",
			"is_regex", "is_regex",
			"compare_everything", "match_all",
			"trigger", "trigger",
			"command", "command",
			new Constant("enabled", new JsonPrimitive(true)));
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
		rename("features.item_saver.tool_saver.damage", "item.tool_saver.damage");
		rename("features.tool_saver.save_non_repairable", "item.tool_saver.save_non_repairable");
		set("item.tool_saver.enabled", new JsonPrimitive(true));
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
		rename("tweaks.chat_mods.stfu_mysterymod.active", "chat.chat_mods.mute_mystery_mod");
		set("chat.chat_mods.enabled", new JsonPrimitive(true));
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
		rename("tweaks.plot_chat_indicator.active", "chat.plot_chat_indicator.enabled");
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
		rename("tweaks.true_sight.entities.entityarmorstand", "render.true_sight.entities.armorstand");
		rename("tweaks.true_sight.entities.entitybat", "render.true_sight.entities.fledermaus");
		rename("tweaks.true_sight.entities.entityblaze", "render.true_sight.entities.blaze");
		rename("tweaks.true_sight.entities.entitycavespider", "render.true_sight.entities.höhlenspinne");
		rename("tweaks.true_sight.entities.entitychicken", "render.true_sight.entities.huhn");
		rename("tweaks.true_sight.entities.entitycow", "render.true_sight.entities.kuh");
		rename("tweaks.true_sight.entities.entitycreeper", "render.true_sight.entities.creeper");
		rename("tweaks.true_sight.entities.entitydragon", "render.true_sight.entities.enderdrache");
		rename("tweaks.true_sight.entities.entityenderman", "render.true_sight.entities.enderman");
		rename("tweaks.true_sight.entities.entityendermite", "render.true_sight.entities.endermite");
		rename("tweaks.true_sight.entities.entityfallingblock", "render.true_sight.entities.falling_block");
		rename("tweaks.true_sight.entities.entityghast", "render.true_sight.entities.ghast");
		rename("tweaks.true_sight.entities.entitygiantzombie", "render.true_sight.entities.riese");
		rename("tweaks.true_sight.entities.entityguardian", "render.true_sight.entities.guardian");
		rename("tweaks.true_sight.entities.entityhorse", "render.true_sight.entities.pferd");
		rename("tweaks.true_sight.entities.entityirongolem", "render.true_sight.entities.eisengolem");
		rename("tweaks.true_sight.entities.entitymagmacube", "render.true_sight.entities.magmawürfel");
		rename("tweaks.true_sight.entities.entitymooshroom", "render.true_sight.entities.pilzkuh");
		rename("tweaks.true_sight.entities.entityocelot", "render.true_sight.entities.ozelot");
		rename("tweaks.true_sight.entities.entitypig", "render.true_sight.entities.schwein");
		rename("tweaks.true_sight.entities.entitypigzombie", "render.true_sight.entities.schweinezombie");
		rename("tweaks.true_sight.entities.entityplayer", "render.true_sight.entities.spieler");
		rename("tweaks.true_sight.entities.entityrabbit", "render.true_sight.entities.hase");
		rename("tweaks.true_sight.entities.entitysheep", "render.true_sight.entities.schaf");
		rename("tweaks.true_sight.entities.entitysilverfish", "render.true_sight.entities.silberfischchen");
		rename("tweaks.true_sight.entities.entityskeleton", "render.true_sight.entities.skelett");
		rename("tweaks.true_sight.entities.entityslime", "render.true_sight.entities.slime");
		rename("tweaks.true_sight.entities.entitysnowman", "render.true_sight.entities.schneegolem");
		rename("tweaks.true_sight.entities.entityspider", "render.true_sight.entities.spinne");
		rename("tweaks.true_sight.entities.entitysquid", "render.true_sight.entities.tintenfisch");
		rename("tweaks.true_sight.entities.entityvillager", "render.true_sight.entities.dorfbewohner");
		rename("tweaks.true_sight.entities.entitywitch", "render.true_sight.entities.hexe");
		rename("tweaks.true_sight.entities.entitywolf", "render.true_sight.entities.wolf");
		rename("tweaks.true_sight.entities.entityzombie", "render.true_sight.entities.zombie");
		rename("tweaks.webhooks.active", "chat.filter_webhooks.enabled");
		rename("tweaks.webhooks.filter", "chat.filter_webhooks.filter");
		rename("features.scammer_list.custom_entries", "player.scammer_list.custom_entries", playerListPatch);
		rename("features.trusted_list.custom_entries", "player.trusted_list.custom_entries", playerListPatch);
		rename("tweaks.player_hider.excluded_players", "render.player_hider.excluded_players", playerListPatch);
		rename("features.show_joins.filter.data", "world.show_joins.players", playerListPatch);
		rename("tweaks.message_skulls.active", "chat.message_skulls.enabled");
		rename("features.map_preview.active", "world.map_preview.enabled");
	}

	private void rename(String oldKey, String newKey, Patcher... patchers) {
		JsonObject oldParent = getParent(oldKey);
		JsonObject newParent = getParent(newKey);
		for (Patcher patcher : patchers)
			call(patcher, oldParent, newParent, oldKey, newKey);

		if (patchers.length == 0 && oldParent.get(getKey(oldKey)) != null)
			newParent.add(getKey(newKey), oldParent.get(getKey(oldKey)));
	}

	private void patchObjectArray(String oldObjectKey, String newObjectKey, Object... changes) {
		JsonObject oldParent = getParent(oldObjectKey);
		JsonObject newParent = getParent(newObjectKey);
		if (oldParent.has(getKey(oldObjectKey))) {
			JsonArray nA = new JsonArray();
			for (JsonElement entry : oldParent.get(getKey(oldObjectKey)).getAsJsonArray()) {
				JsonObject oldEntry = entry.getAsJsonObject();
				JsonObject newEntry = new JsonObject();
				LinkedList<Object> lt = new LinkedList<>(Arrays.asList(changes));
				while (!lt.isEmpty()) {
					if (lt.peek() instanceof Constant) {
						Constant constant = (Constant) lt.pop();
						newEntry.add(constant.key, constant.value);
						continue;
					}

					String oldEntryKey = (String) lt.pop();
					String newEntryKey = (String) lt.pop();
					call(lt.peek() instanceof Patcher ? lt.pop() : newEntryKey, oldEntry, newEntry, oldEntryKey, newEntryKey);
				}
				nA.add(newEntry);
			}
			newParent.add(getKey(newObjectKey), nA);
		}
	}

	private Patcher enumPatch(String... changes) {
		return (oldParent, newParent, oldKey, newKey) -> {
			for (int i = 0; i < changes.length; i += 2) {
				if (changes[i].equalsIgnoreCase(oldParent.get(oldKey).getAsString())) {
					newParent.addProperty(newKey, changes[i + 1]);
					return;
				}
			}
		};
	}

	private void call(Object patch, JsonObject oldParent, JsonObject newParent, String oldKey, String newKey) {
		if (!oldParent.has(oldKey))
			return;

		if (patch instanceof Patcher)
			((Patcher) patch).patch(oldParent, newParent, oldKey, newKey);
		else if (patch instanceof String)
			newParent.add((String) patch, oldParent.get(oldKey));
	}

	private final Patcher moduleTimerFormatPatch = (oldParent, newParent, oldKey, newKey) -> {
		if (oldParent.get(oldKey).getAsBoolean())
			newParent.addProperty(newKey, "SHORT");
		else
			newParent.addProperty(newKey, "LONG");
	};

	private final Patcher playerListPatch = (oldParent, newParent, oldKey, newKey) -> {
		JsonArray a = new JsonArray();
		for (JsonElement entry : oldParent.get(oldKey).getAsJsonArray())
			a.add(new JsonPrimitive(entry.getAsJsonObject().get("uuid").getAsString().replaceAll(".{8}.{4}.{4}.{4}.{12}", "$1-$2-$3-$4-$5")));
		newParent.add(newKey, a);
	};

	private final Patcher chatMenuCommandPatch = (oldParent, newParent, oldKey, newKey) -> {
		String command = oldParent.get("value").getAsString().replaceAll("(?i)%player%", "%name%");
		newParent.addProperty("command", command);
	};

	private final Patcher chatMenuIconPatch = (oldParent, newParent, oldKey, newKey) -> {
		String newValue = null;
		switch (oldParent.get("icon_type").getAsString()) {
			case "item":
				newValue = oldParent.get(oldKey).getAsString();
				break;
			case "image":
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(scale(ImageIO.read(new File(oldParent.get(oldKey).getAsString()))), "PNG", out);
					newValue = Base64.getEncoder().encodeToString(out.toByteArray());
					newParent.addProperty("icon_name", new File(oldParent.get(oldKey).getAsString()).getName());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		newParent.addProperty("icon", newValue);
	};

	private BufferedImage scale(BufferedImage img) {
		if (img.getHeight() > 64 || img.getWidth() > 64) {
			float scaleFactor = (64f / (float) Math.max(img.getHeight(), img.getWidth()));
			Image scaledImg = (img.getScaledInstance((int) (img.getWidth() * scaleFactor), (int) (img.getHeight() * scaleFactor), Image.SCALE_DEFAULT));
			img = new BufferedImage(scaledImg.getWidth(null), scaledImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.drawImage(scaledImg, 0, 0, null);
			g.dispose();
			return img;
		}
		return img;
	}

	private interface Patcher {
		void patch(JsonObject oldParent, JsonObject newParent, String oldKey, String newKey);
	}

	private static class Constant {
		private final String key;
		private final JsonElement value;

		public Constant(String key, JsonElement value) {
			this.key = key;
			this.value = value;
		}
	}

	private JsonObject getParent(String path) {
		String[] parts = path.split("\\.");
		JsonObject obj = config;
		for (int i = 0; i < parts.length - 1; i++) {
			if (!obj.has(parts[i]) || !(obj.get(parts[i]).isJsonObject()))
				obj.add(parts[i], new JsonObject());
			obj = obj.get(parts[i]).getAsJsonObject();
		}
		return obj;
	}

	private String getKey(String path) {
		return ArrayUtil.last(path.split("\\."));
	}

	private void set(String path, JsonElement value) {
		getParent(path).add(getKey(path), value);
	}

}