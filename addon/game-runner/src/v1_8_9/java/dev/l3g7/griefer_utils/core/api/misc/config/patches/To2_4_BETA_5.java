/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_4_BETA_5 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.4-BETA-5");
	}

	@Override
	public void patch() {
		// Revamped setting layout

		// Reordering
		moveBulk("chat", "chat.ingoing", "auto_unnick", "enlighten", "chat_mods", "chat_menu", "chat_reactor", "chat_time", "real_money", "interactable_messages", "message_skulls", "no_magic_text");
		moveBulk("chat", "chat.outgoing", "anti_command_choker", "better_switch_command", "command_logger", "command_pie_menu", "command_suggestions", "color_preview", "fix_kicks", "split_long_messages", "multi_hotkey", "plot_chat_indicator", "calculator", "slow_chat_cooldown");
		moveBulk("chat", "chat.chat_filter", "unlock_chat_filters", "chat_filter_templates", "filter_webhooks");

		move("chat", "interactable_friends_menu", "gui.griefer_games");
		moveBulk("player", "gui.griefer_games", "better_home_menu", "better_plot_menu", "cooldown_notifications", "interactable_profiles");
		moveBulk("world", "gui.griefer_games", "better_hopper", "better_adventurer", "better_job_exchange", "n_p_c_entity_ghost_hand", "orb_seller_fix");

		rename("byte_and_bit", "gui.integrations.byte_and_bit");
		rename("griefer_info", "gui.integrations.griefer_info");
		move("world", "b_s_f", "gui.integrations");
		move("item", "book_fix", "gui.books");
		move("player", "suppress_books", "gui.books");
		move("player", "laby_mod_switcher_fix", "gui");
		move("world", "item_search", "gui");

		moveBulk("world", "item.item_info", "map_preview", "skull_preview");
		moveBulk("item", "item.item_saver", "armor_break_warning", "chest_saver", "orb_saver", "particle_saver", "prefix_saver", "border_saver", "tool_saver", "head_saver");
		move("item.item_saver", "entries", "item.item_saver.specific_item_saver");
		move("item.item_saver", "enabled", "item.item_saver.specific_item_saver");
		move("item.item_saver", "display_icon", "item.item_saver.specific_item_saver");
		move("world", "dragon_egg_saver", "item.item_saver");

		moveBulk("render", "render.light", "full_bright", "light_bug_e_s_p", "light_level_overlay");
		moveBulk("world", "render.skulls", "head_texture_fix", "skull_enchantment_fix");
		move("world", "bugged_maps_fix", "render");

		moveBulk("player", "player.movement", "auto_sprint", "safer_sneak");
		moveBulk("render", "player.name_tags", "clan_tags", "name_tag_prefix_sync", "show_nametags_through_walls");
		move("chat", "standard_prefixes", "player.name_tags");
		moveBulk("render", "player", "player_hider", "remove_walking_mini_mes");
		move("world", "self_disguise", "player");

		moveBulk("item.inventory_tweaks", "world.building", "inventory_block_selection", "block_refill");
		move("player", "aligner", "world.building");
		move("item", "auto_tool", "world.building");
		move("render", "show_barriers", "world.building");
		moveBulk("world", "world.building", "ghost_blocks_fix", "chunk_indicator", "easy_place", "nature_border_indicator", "redstone_helper", "better_schematica", "better_sign");
		moveBulk("world", "world.joining", "auto_portal", "portal_cooldown");
		moveBulk("player", "world.scoreboard", "bank_scoreboard", "orb_scoreboard", "cleanup_scoreboard");
		move("render", "hide_scoreboard_in_f3", "world.scoreboard");
		move("render", "trajectories", "world");

		// Name changes
		rename("chat.chat_filter", "filter_webhooks", "chat_filter_webhooks");
		rename("chat.ingoing", "chat_mods", "chat_cleanup");
		rename("chat.ingoing", "no_magic_text", "remove_magic_text");
		rename("chat.outgoing", "better_switch_command", "better_switch");
		rename("gui", "laby_mod_switcher_fix", "fix_laby3_switcher");
		rename("gui.books", "book_fix", "fix_books");
		rename("gui.griefer_games", "orb_seller_fix", "fix_orb_seller");
		rename("gui.griefer_games", "n_p_c_entity_ghost_hand", "n_p_c_ghost_hand");
		rename("item.item_info", "skull_preview", "head_preview");
		rename("player", "afk_nick", "auto_nick");
		rename("player", "player_hider", "hide_players");
		rename("player.name_tags", "standard_prefixes", "default_prefixes");
		rename("player.name_tags", "name_tag_prefix_sync", "name_tags_with_prefix");
		rename("player.name_tags", "show_nametags_through_walls", "show_name_tags_through_walls");
		rename("render", "bugged_maps_fix", "fix_unsynced_maps");
		rename("render", "no_overlay", "hide_overlays");
		rename("render", "true_sight", "show_invisible_entities");
		rename("render.skulls", "head_texture_fix", "fix_head_textures");
		rename("render.skulls", "skull_enchantment_fix", "fix_tile_entity_enchantments");
		rename("world.building", "block_refill", "auto_block_refill");
		rename("world.building", "better_sign", "better_sign_edit");
		rename("world.building", "ghost_blocks_fix", "fix_ghost_blocks");
		rename("world.building", "inventory_block_selection", "select_inventory_blocks");
		rename("world.building", "chunk_indicator", "show_chunk_borders");
		rename("world.building", "nature_border_indicator", "show_nature_borders");

		// Category changes
		rename("chat", "active", "enabled");
		rename("chat.ingoing", "active", "enabled");
		rename("chat.outgoing", "active", "enabled");
		rename("chat.chat_filter", "active", "enabled");
		rename("gui", "active", "enabled");
		rename("gui.books", "active", "enabled");
		rename("gui.griefer_games", "active", "enabled");
		rename("gui.integrations", "active", "enabled");
		rename("item", "active", "enabled");
		rename("item.inventory_tweaks", "active", "enabled");
		rename("item.item_info", "active", "enabled");
		rename("item.item_saver", "active", "enabled");
		rename("render", "active", "enabled");
		rename("render.light", "active", "enabled");
		rename("render.skulls", "active", "enabled");
		rename("player", "active", "enabled");
		rename("player.movement", "active", "enabled");
		rename("player.name_tags", "active", "enabled");
		rename("world", "active", "enabled");
		rename("world.building", "active", "enabled");
		rename("world.joining", "active", "enabled");
		rename("world.scoreboard", "active", "enabled");

		// Acknowledged changes
		var settings = get("settings");
		if (settings.has("acknowledged_changes")) {
			JsonArray patchedChanges = new JsonArray();
			for (JsonElement element : settings.getJsonArray("acknowledged_changes")) {
				String key = element.getAsString();
				patchedChanges.add(new JsonPrimitive(switch (key) {
					case "chat.calculator.inline_calculation" -> "chat.outgoing.calculator.inline_calculation";
					case "chat.chat_mods.remove_broadcast" -> "chat.ingoing.chat_cleanup.remove_broadcast";
					case "chat.chat_mods.remove_hero_highlights" -> "chat.ingoing.chat_cleanup.remove_hero_highlights";
					case "chat.command_suggestions.enabled" -> "chat.outgoing.command_suggestions.enabled";
					case "chat.fix_kicks.enabled" -> "chat.outgoing.fix_kicks.enabled";
					case "chat.interactable_friends_menu.enabled" ->
						"gui.griefer_games.interactable_friends_menu.enabled";
					case "chat.plot_chat_indicator.replace_global_chat" ->
						"chat.outgoing.plot_chat_indicator.replace_global_chat";
					case "chat.real_money.highlight_cents" -> "chat.ingoing.real_money.highlight_cents";
					case "player.better_plot_menu.enabled" -> "gui.griefer_games.better_plot_menu.enabled";
					case "player.cleanup_scoreboard.enabled" -> "world.scoreboard.cleanup_scoreboard.enabled";
					case "render.show_nametags_through_walls.enabled" ->
						"player.name_tags.show_name_tags_through_walls.enabled";
					case "world.better_hopper.show_fast_tick" -> "gui.griefer_games.better_hopper.show_fast_tick";
					case "world.better_hopper.sneak_mode.hopper_with_held_item_fix" ->
						"gui.griefer_games.better_hopper.sneak_mode.hopper_with_held_item_fix";
					case "world.nature_border_indicator.enabled" -> "world.building.show_nature_borders.enabled";
					case "world.optimize_chunks.enabled" -> "world.optimize_chunks.enabled";
					default -> key;
				}));
			}

			settings.set("acknowledged_changes", patchedChanges);
		}
	}

	private void moveBulk(String oldParent, String newParent, String... keys) {
		for (String key : keys)
			move(oldParent, key, newParent);
	}

	private void move(String oldParent, String key, String newParent) {
		rename(oldParent + "." + key, newParent + "." + key);
	}

}
