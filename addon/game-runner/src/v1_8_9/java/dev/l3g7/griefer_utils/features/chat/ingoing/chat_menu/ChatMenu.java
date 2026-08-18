/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.CopyTextEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.EntryDisplaySetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu.ChatMenuBridge.chatMenuVersioned;
import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry.Action.*;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ChatMenu extends Feature {

	protected static final CopyTextEntry COPY_TEXT_ENTRY = new CopyTextEntry();

	protected static final List<ChatMenuEntry> DEFAULT_ENTRIES = ImmutableList.of(
		new ChatMenuEntry("Profil öffnen", RUN_CMD, "/profil %name%", "wooden_board"),
		new ChatMenuEntry("Namensverlauf", CONSUMER, "/gu:name_history %name%", "name_tag_yellow"),
		new ChatMenuEntry("Namen kopieren", CONSUMER, "/gu:copy %name%", "name_tag_yellow"),
		new ChatMenuEntry("Im Forum suchen", OPEN_URL, "https://forum.griefergames.de/search/?q=%name%", "griefer_games"),
		new ChatMenuEntry("Inventar öffnen", RUN_CMD, "/invsee %name%", "bundle"),
		new ChatMenuEntry("Ausrüstung ansehen", RUN_CMD, "/view %name%", "diamond_chestplate"),
		new ChatMenuEntry("EC öffnen", RUN_CMD, "/ec %name%", "chest_ender")
	);

	protected static ChatMenuRenderer renderer = null;

	protected static final EntryAddSetting newEntrySetting = EntryAddSetting.create()
		.name("Neuen Menüpunkt erstellen")
		.callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddChatMenuEntryGui(null, Minecraft.getMinecraft().currentScreen)));

	@MainElement(configureSubSettings = false)
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Chatmenü")
		.description("Öffnet ein Chatmenü bei Rechtsklick auf einen Spieler im Chat.")
		.icon("player_menu");

	public ChatMenu() {
		loadEntries();
		List<BaseSetting<?>> settings = new ArrayList<>();

		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			settings.add(
				entry.setIcon(SwitchSetting.create())
					.name(entry.name)
					.callback(v -> entry.enabled = v)
					.defaultValue(true)
					.set(entry.enabled)
					.config("chat.ingoing.chat_menu.entries." + entry.name));
		}

		settings.add(COPY_TEXT_ENTRY.getSetting());
		settings.add(newEntrySetting);

		enabled.subSettings(settings.toArray(new BaseSetting[0]));

		String path = "chat.ingoing.chat_menu.entries.custom";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				SwitchSetting setting = chatMenuVersioned.createEntry(ChatMenuEntry.fromJson(jsonElement.getAsJsonObject()));
				enabled.addSetting(setting);
				setting.create(enabled);
			}

			saveEntries();
		}
	}

	public static ChatMenu get() {
		return get(ChatMenu.class);
	}

	public BaseSetting<?> getAddSetting() {
		return newEntrySetting;
	}

	public void saveEntries() {
		for (ChatMenuEntry entry : DEFAULT_ENTRIES)
			Config.set("chat.ingoing.chat_menu.entries." + entry.name, new JsonPrimitive(entry.enabled));

		JsonArray array = new JsonArray();
		for (ChatMenuEntry customEntry : getCustom())
			if (customEntry.completed)
				array.add(customEntry.toJson());

		Config.set("chat.ingoing.chat_menu.entries.custom", array);
		Config.save();
	}

	private void loadEntries() {
		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			String path = "chat.ingoing.chat_menu.entries." + entry.name;

			if (Config.has(path))
				entry.enabled = Config.get(path).getAsBoolean();
		}
	}

	@EventListener
	public void onRender(TickEvent.RenderTickEvent event) {
		if (renderer != null)
			renderer.render();
	}

	@EventListener
	public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (renderer != null && renderer.onMouse()) {
			renderer = null;
			event.cancel();
			return;
		}

		if (!Mouse.getEventButtonState())
			return;

		if (renderer != null && renderer.outOfBox())
			renderer = null;

		if (Mouse.getEventButton() != 1 || !chatMenuVersioned.isChatOpen())
			return;

		Pair<IChatComponent, IChatComponent> component = chatMenuVersioned.getHoveredComponent();
		if (component == null)
			return; // Didn't click on a line

		String name = null;

		for (Pattern p : new Pattern[]{GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, CLANCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN, GLOBAL_CHAT_PATTERN}) {
			Matcher matcher = p.matcher(component.b.getFormattedText());
			if (!matcher.find())
				continue;

			name = matcher.group("name").replaceAll("§.", "");
			break;
		}

		if (name == null)
			return;

		List<ChatMenuEntry> entries = new ArrayList<>();
		DEFAULT_ENTRIES.forEach(e -> {if (e.enabled) entries.add(e);});
		if (COPY_TEXT_ENTRY.enabled) entries.add(COPY_TEXT_ENTRY);
		getCustom().forEach(e -> {if (e.enabled) entries.add(e);});

		name = name.replaceAll("§.", "").trim();
		String realName = NameCache.ensureRealName(name);
		if (realName == null)
			realName = name;

		renderer = new ChatMenuRenderer(entries, realName, component.a, component.b);
		event.cancel();
	}

	@EventListener
	public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (renderer == null)
			return;

		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == 1) {
			renderer = null;
			event.cancel();
		}

		mc().dispatchKeypresses();
	}

	public static List<ChatMenuEntry> getCustom() {
		return enabled.getChildSettings()
			.stream()
			.filter(e -> e instanceof EntryDisplaySetting)
			.map(e -> ((EntryDisplaySetting) e).getEntry())
			.collect(Collectors.toList());
	}

	@Bridged
	public interface ChatMenuBridge {

		ChatMenuBridge chatMenuVersioned = FileProvider.getBridge(ChatMenuBridge.class);

		EntryDisplaySetting createEntry(ChatMenuEntry entry);

		SwitchSetting createCopyEntry(CopyTextEntry target);

		boolean isChatOpen();

		Pair<IChatComponent, IChatComponent> getHoveredComponent();

	}
}