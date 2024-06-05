/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby4;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.util.ChatLineUtil;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.ChatInputOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_4)
public class ChatMenu extends Feature {

	protected static final CopyTextEntry COPY_TEXT_ENTRY = new CopyTextEntry();

	protected static final List<ChatMenuEntry> DEFAULT_ENTRIES = ImmutableList.of(
		new ChatMenuEntry("Profil öffnen", ChatMenuEntry.Action.RUN_CMD, "/profil %name%", "wooden_board"),
		new ChatMenuEntry("Namensverlauf", ChatMenuEntry.Action.CONSUMER, (Consumer<String>) ChatMenu::openNameHistory, "yellow_name"),
		new ChatMenuEntry("Namen kopieren", ChatMenuEntry.Action.CONSUMER, (Consumer<String>) ChatMenu::copyToClipboard, "yellow_name"),
		new ChatMenuEntry("Im Forum suchen", ChatMenuEntry.Action.OPEN_URL, "https://forum.griefergames.de/search/?q=%name%", "earth_grid"),
		new ChatMenuEntry("Inventar öffnen", ChatMenuEntry.Action.RUN_CMD, "/invsee %name%", "bundle"),
		new ChatMenuEntry("Ausrüstung ansehen", ChatMenuEntry.Action.RUN_CMD, "/view %name%", new ItemStack(Items.iron_chestplate)),
		new ChatMenuEntry("EC öffnen", ChatMenuEntry.Action.RUN_CMD, "/ec %name%", "chest")
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
			settings.add(SwitchSetting.create()
				.name(entry.name)
				.callback(v -> entry.enabled = v)
				.defaultValue(entry.enabled)
				.config("chat.chat_menu.entries." + entry.name)
				.icon(entry.icon));
		}

		settings.add(COPY_TEXT_ENTRY.getSetting());
		settings.add(newEntrySetting);

		enabled.subSettings(settings.toArray(new BaseSetting[0]));


		String path = "chat.chat_menu.entries.custom";
		if (Config.has(path))
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray())
				enabled.addSetting(new EntryDisplaySetting(ChatMenuEntry.fromJson(jsonElement.getAsJsonObject())));
	}

	public static void saveEntries() {
		for (ChatMenuEntry entry : DEFAULT_ENTRIES)
			Config.set("chat.chat_menu.entries." + entry.name, new JsonPrimitive(entry.enabled));

		JsonArray array = new JsonArray();
		for (ChatMenuEntry customEntry : getCustom())
			if (customEntry.completed)
				array.add(customEntry.toJson());

		Config.set("chat.chat_menu.entries.custom", array);
		Config.save();
	}

	private void loadEntries() {
		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			String path = "chat.chat_menu.entries." + entry.name;

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

		if (Mouse.getEventButton() != 1 || !(Laby4Util.getActivity() instanceof ChatInputOverlay))
			return;

		IChatComponent icc = ChatLineUtil.getUnmodifiedIChatComponent(ChatLineUtil.getHoveredComponent());
		if (icc == null)
			return; // Didn't click on a line

		String name = null;

		for (Pattern p : new Pattern[]{GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN, GLOBAL_CHAT_PATTERN}) {
			Matcher matcher = p.matcher(icc.getFormattedText());
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

		renderer = new ChatMenuRenderer(entries, realName, ChatLineUtil.getHoveredComponent(), icc);
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

	private static void openNameHistory(String name) {
		if (name.startsWith("!")) {
			labyBridge.notifyMildError("Von Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		labyBridge.openNameHistory(name);
	}

	static void copyToClipboard(String text) {
		Laby.labyAPI().minecraft().chatExecutor().copyToClipboard(text);
		labyBridge.notify("\"" + text + "\"", "wurde in die Zwischenablage kopiert.");
	}

	public static List<ChatMenuEntry> getCustom() {
		return enabled.getChildSettings()
			.stream()
			.filter(e -> e instanceof EntryDisplaySetting)
			.map(e -> ((EntryDisplaySetting) e).entry)
			.collect(Collectors.toList());
	}

	/**
	 * Ensures newEntrySetting is always the last child.
	 */
	@EventListener(triggerWhenDisabled = true)
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != enabled)
			return;

		Iterator<Widget> it = event.settings().getChildren().iterator();
		SettingWidget newEntryWidget = null;
		while (it.hasNext()) {
			Widget w = it.next();
			if (w instanceof SettingWidget s && s.setting() == newEntrySetting) {
				newEntryWidget = s;
				it.remove();
				break;
			}
		}

		event.settings().addChild(newEntryWidget);
	}

}