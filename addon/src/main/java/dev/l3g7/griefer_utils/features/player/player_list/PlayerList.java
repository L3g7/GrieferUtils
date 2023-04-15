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

package dev.l3g7.griefer_utils.features.player.player_list;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.DisplayNameGetEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.player.player_list.PlayerList.MarkAction.*;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.event.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.event.HoverEvent.Action.SHOW_TEXT;

/**
 * An indicator for players on a <a href="https://scammer-radar.de/">ScammerRadar</a> list.
 * @see ScammerList
 * @see TrustedList
 */
public abstract class PlayerList extends Feature {

	public static final boolean enforce = System.getProperty("enforceSR") != null;

	private static final Pattern PROFILE_TITLE_PATTERN = Pattern.compile(String.format("^§6Profil von §e%s§r$", Constants.FORMATTED_PLAYER_NAME_PATTERN));

	private final String name, icon;
	private final ModColor color;
	private final int paneType; // The glass pane color in /profil

	// List entries
	private final List<UUID> uuids = new ArrayList<>();
	private final List<String> names = new ArrayList<>();

	// List settings
	public final DropDownSetting<MarkAction> tabAction = new DropDownSetting<>(MarkAction.class)
		.name("in Tabliste")
		.icon("tab_list")
		.defaultValue(ICON)
		.callback(a -> TabListEvent.updatePlayerInfoList());

	public final DropDownSetting<MarkAction> chatAction = new DropDownSetting<>(MarkAction.class)
		.name("in Chat")
		.icon("speech_bubble")
		.defaultValue(ICON);

	public final DropDownSetting<MarkAction> displayNameAction = new DropDownSetting<>(MarkAction.class)
		.name("Vor Nametag")
		.icon("yellow_name")
		.defaultValue(ICON);

	public final BooleanSetting showInProfile = new BooleanSetting()
		.name("In /profil anzeigen")
		.icon("info")
		.defaultValue(true);

	public final PlayerListSetting customEntries = new PlayerListSetting()
		.callback(l -> TabListEvent.updatePlayerInfoList());

	@MainElement
	public final BooleanSetting enabled = new BooleanSetting() {

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			if (enforce)
				super.draw(x, y, maxX, maxY, mouseX, mouseY);
		}

		@Override
		public int getEntryHeight() {
			return enforce ? super.getEntryHeight() : 0;
		}

	};

	public PlayerList(String name, String chatIcon, Object settingIcon, ModColor color, int paneType, String url) {
		enabled
			.name(name + "liste")
			.description("Markiert Spieler in der ScammerRadar-" + name + "liste.")
			.icon(settingIcon)
			.callback(v -> TabListEvent.updatePlayerInfoList())
			.subSettings(tabAction, chatAction, displayNameAction, showInProfile, new HeaderSetting(), new HeaderSetting("Eigene " + name), customEntries);

		this.name = name;
		customEntries.setContainer(enabled);

		this.icon = chatIcon;
		this.color = color;
		this.paneType = paneType;

		// Read entries from url
		IOUtil.read(url)
			.asJsonArray(entries -> entries.forEach(e -> {
				JsonObject entry = e.getAsJsonObject();
				uuids.add(UUID.fromString(entry.get("uuid").getAsString()));
				names.add(entry.get("name").getAsString());
			}))
			.orElse(() -> {
				SettingsElement setting = getMainElement();
				setting.setDisplayName("§c§o" + setting.getDisplayName());
				setting.setDescriptionText(setting.getDisplayName() + " konnte nicht geladen werden!");
			});
	}

	@Override
	public void init() {
		super.init();
		getCategory().getSetting().addCallback(v -> TabListEvent.updatePlayerInfoList());
		if (!enforce)
			enabled.set(false);
	}

	/**
	 * The event listener handling display names.
	 * @see PlayerList#displayNameAction
	 */
	@EventListener(priority = EventPriority.LOW)
	public void onDisplayNameRender(DisplayNameGetEvent event) {
		if (displayNameAction.get() == DISABLED)
			return;

		if (uuids.contains(event.player.getUniqueID()) || names.contains(event.player.getName()) || customEntries.contains(event.player.getName(), event.player.getUniqueID()))
			event.displayName = toComponent(displayNameAction.get()).appendSibling(event.displayName);
	}

	/**
	 * The event listener handling the tab list.
	 * @see PlayerList#tabAction
	 */
	@EventListener
	public void onTabNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (tabAction.get() == DISABLED)
			return;

		if (uuids.contains(event.profile.getId()) || names.contains(event.profile.getName()) || customEntries.contains(event.profile.getName(), event.profile.getId()))
			event.component = toComponent(tabAction.get()).appendSibling(event.component);
	}

	/**
	 * The event listener handling incoming messages.
	 * @see PlayerList#chatAction
	 */
	@EventListener
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
			if (!uuids.contains(uuid) && !names.contains(name) && !customEntries.contains(name, uuid))
				return;

			ChatStyle style = new ChatStyle();

			// Add /profil command on click
			style.setChatClickEvent(new ClickEvent(RUN_COMMAND, "/profil " + NameCache.ensureRealName(name)));

			// Add description
			style.setChatHoverEvent(new HoverEvent(SHOW_TEXT, new ChatComponentText("§" + color.getColorChar() + "§l" + this.name)));

			// Update message
			event.message.getSiblings().add(0,  toComponent(chatAction.get()).setChatStyle(style));
		});
	}

	/**
	 * The event listener handling /profil.
	 * @see PlayerList#showInProfile
	 */
	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		if (!showInProfile.get())
			return;

		// Check if chest is open
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inventory = Reflection.get(mc().currentScreen, "lowerChestInventory");

		// Check if chest is /profil using title
		Matcher matcher = PROFILE_TITLE_PATTERN.matcher(inventory.getDisplayName().getFormattedText());
		if (!matcher.matches())
			return;

		// Check if player should be marked
		String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
		UUID uuid = name.contains("~") ? NameCache.getUUID(name) : PlayerUtil.getUUID(name);
		if (!uuids.contains(uuid) && !names.contains(name) && !customEntries.contains(name, uuid))
			return;

		// Construct item
		ItemStack indicatorPane = new ItemStack(Blocks.stained_glass_pane, 1, paneType);
		indicatorPane.setStackDisplayName("§" + color.getColorChar() + "§l" + this.name);

		// Replace every glass pane with indicatorPane
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack slot = inventory.getStackInSlot(i);
			if (slot != null && slot.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
				inventory.setInventorySlotContents(i, indicatorPane);
		}
	}

	enum MarkAction {

		TAG("Als Text"),  // Mark an entry using a tag,   e.g. [SCAMMER]
		ICON("Als Icon"), // Mark an entry using an icon, e.g. [⚠]
		DISABLED("Aus");  // Don't mark an entry

		final String name;
		MarkAction(String name) {
			this.name = name;
		}

	}

	/**
	 * Converts a {@link MarkAction} into a {@link ChatComponentText}.
	 */
	private ChatComponentText toComponent(MarkAction action) {
		if (action == ICON)
			return new ChatComponentText(color + "[" + icon + "] ");
		if (action == TAG)
			return new ChatComponentText(color + "[§l" + name.toUpperCase() + color + "] ");
		return new ChatComponentText("");
	}

}
