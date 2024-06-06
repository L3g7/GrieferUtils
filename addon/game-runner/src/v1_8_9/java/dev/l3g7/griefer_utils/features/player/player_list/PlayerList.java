/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.player_list;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.DisplayNameGetEvent;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby3;
import dev.l3g7.griefer_utils.core.settings.player_list.PlayerListSettingLaby4;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.features.player.player_list.PlayerList.MarkAction.*;
import static net.minecraft.event.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.event.HoverEvent.Action.SHOW_TEXT;

public abstract class PlayerList extends Feature {

	private final String message;
	private final String icon;
	private final EnumChatFormatting color;
	private final int paneType; // The glass pane color in /profil

	// List entries
	protected final List<UUID> uuids = new ArrayList<>();
	protected final List<String> names = new ArrayList<>();

	// List settings
	public final DropDownSetting<MarkAction> tabAction = DropDownSetting.create(MarkAction.class)
		.name("in Tabliste")
		.description("Ob Spieler in dieser Liste in der Tabliste markiert werden sollen.")
		.icon("tab_list")
		.defaultValue(ICON)
		.callback(TabListEvent::updatePlayerInfoList);

	public final DropDownSetting<MarkAction> chatAction = DropDownSetting.create(MarkAction.class)
		.name("in Chat")
		.description("Ob Spieler in dieser Liste im Chat markiert werden sollen.")
		.icon("speech_bubble")
		.defaultValue(ICON);

	public final DropDownSetting<MarkAction> displayNameAction = DropDownSetting.create(MarkAction.class)
		.name("Vor Nametag")
		.description("Ob Spieler in dieser Liste eine Markierung vor ihrem Namen haben sollen.")
		.icon("yellow_name")
		.defaultValue(ICON);

	public final SwitchSetting showInProfile = SwitchSetting.create()
		.name("In /profil anzeigen")
		.description("Ob das Profil von Spielern in dieser Liste markiert werden soll.")
		.icon("info")
		.defaultValue(true);

	public final AbstractSetting<?, List<PlayerListEntry>> customEntries;

	@MainElement
	public final SwitchSetting enabled = SwitchSetting.create()
		.callback(TabListEvent::updatePlayerInfoList);

	public PlayerList(String name, String description, String chatIcon, Object settingIcon, String entryDescription, EnumChatFormatting color, int paneType, String message, String url) {
		if (LABY_4.isActive())
			customEntries = new PlayerListSettingLaby4()
				.callback(TabListEvent::updatePlayerInfoList);
		else
			customEntries = new PlayerListSettingLaby3()
				.callback(TabListEvent::updatePlayerInfoList);

		enabled
			.name(name)
			.description(description)
			.icon(settingIcon)
			.subSettings(tabAction, chatAction, displayNameAction, showInProfile, HeaderSetting.create(), customEntries.name(entryDescription).icon(settingIcon));

		this.message = message;
		this.icon = chatIcon;
		this.color = color;
		this.paneType = paneType;

		if (url != null) {
			// Read entries from url
			IOUtil.read(url)
				.asJsonArray(entries -> entries.forEach(e -> {
					JsonObject entry = e.getAsJsonObject();
					uuids.add(UUID.fromString(entry.get("uuid").getAsString()));
					names.add(entry.get("name").getAsString());
				}))
				.orElse(() -> {
					BaseSetting<?> setting = getMainElement();
					setting.name("§c§o" + setting.name());
					setting.description(setting.name() + " konnte nicht geladen werden!");
				});
		}
	}

	@Override
	public void init() {
		super.init();
		getCategory().callback(TabListEvent::updatePlayerInfoList);
	}

	/**
	 * The event listener handling display names.
	 *
	 * @see PlayerList#displayNameAction
	 */
	@EventListener(priority = Priority.LOW)
	public void onDisplayNameRender(DisplayNameGetEvent event) {
		if (displayNameAction.get() == DISABLED)
			return;

		if (shouldMark(event.player.getName(), event.player.getUniqueID()))
			event.displayName = toComponent(displayNameAction.get()).appendSibling(event.displayName);
	}

	/**
	 * The event listener handling the tab list.
	 *
	 * @see PlayerList#tabAction
	 */
	@EventListener
	public void onTabNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (tabAction.get() == DISABLED)
			return;

		if (shouldMark(event.profile.getName(), event.profile.getId()))
			event.component = toComponent(tabAction.get()).appendSibling(event.component);
	}

	/**
	 * The event listener handling incoming messages.
	 *
	 * @see PlayerList#chatAction
	 */
	@EventListener(priority = Priority.LOW)
	public void onMessageModify(MessageModifyEvent event) {
		if (chatAction.get() == DISABLED)
			return;

		// Check if message is GLOBAL_RECEIVE, PLOTCHAT_RECEIVE, MESSAGE_RECEIVE, MESSAGE_SEND or GLOBAL_CHAT
		Constants.MESSAGE_PATTERNS.stream().map(p -> p.matcher(event.original.getFormattedText())).filter(Matcher::matches).findFirst().ifPresent(matcher -> {

			// Check if player should be marked
			String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
			if (name == null) // Nicked player in global chat
				return;

			UUID uuid = name.contains("~") ? NameCache.getUUID(name) : PlayerUtil.getUUID(name);
			if (!shouldMark(name, uuid))
				return;

			ChatStyle style = new ChatStyle();

			// Add /profil command on click
			style.setChatClickEvent(new ClickEvent(RUN_COMMAND, "/profil " + NameCache.ensureRealName(name)));

			// Add description
			style.setChatHoverEvent(new HoverEvent(SHOW_TEXT, new ChatComponentText(this.message)));

			// Update message
			event.setMessage(toComponent(chatAction.get()).setChatStyle(style).appendSibling(event.message));
		});
	}

	/**
	 * The event listener handling /profil.
	 *
	 * @see PlayerList#showInProfile
	 */
	@EventListener
	public void onTick(GuiModifyItemsEvent event) {
		if (!showInProfile.get())
			return;

		if (!event.getTitle().startsWith("§6Profil"))
			return;

		ItemStack skull = event.getItem(13);
		if (skull == null || skull.getItem() != Items.skull)
			return;

		// Check if player should be marked
		String name = skull.getDisplayName().substring(2);
		UUID uuid = name.contains("~") ? NameCache.getUUID(name) : PlayerUtil.getUUID(name);
		if (!shouldMark(name, uuid))
			return;

		// Construct item
		ItemStack indicatorPane = new ItemStack(Blocks.stained_glass_pane, 1, paneType);
		indicatorPane.setStackDisplayName(this.message);

		// Replace every glass pane with indicatorPane
		for (int i = 0; i < 45; i++) {
			ItemStack stack = event.getItem(i);
			if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
				event.setItem(i, indicatorPane);
		}
	}

	public boolean shouldMark(String name, UUID uuid) {
		if (name == null && uuid == null)
			return false;

		if (uuids.contains(uuid) || names.contains(name))
			return true;

		for (PlayerListEntry entry : customEntries.get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.id) : name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}


	public enum MarkAction implements Named {

		TAG("Als Text"),  // Mark an entry using a tag,   e.g. [SCAMMER]
		ICON("Als Icon"), // Mark an entry using an icon, e.g. [⚠]
		DISABLED("Aus");  // Don't mark an entry

		final String name;

		MarkAction(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	/**
	 * Converts a {@link MarkAction} into a {@link ChatComponentText}.
	 */
	public ChatComponentText toComponent(MarkAction action) {
		if (action == ICON)
			return new ChatComponentText(color + "[" + icon + "] ");
		if (action == TAG)
			return new ChatComponentText(color + "[§l" + message.toUpperCase() + color + "] ");
		return new ChatComponentText("");
	}

}
