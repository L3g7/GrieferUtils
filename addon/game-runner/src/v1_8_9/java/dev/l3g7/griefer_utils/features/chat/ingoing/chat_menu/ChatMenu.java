/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.KeyboardInputEvent;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.MouseInputEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.CopyTextEntry;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu.ChatMenuBridge.chatMenuBridge;

@Singleton
public class ChatMenu extends Feature {

	private final CopyTextEntry COPY_TEXT_ENTRY = new CopyTextEntry();

	private ChatMenuRenderer activeRenderer = null;

	private final ListSetting<ChatMenuEntry> entries = ListSetting.create(ChatMenuEntry.class)
		.name("Einträge")
		.icon("player_menu")
		.customEdit(e -> mc().displayGuiScreen(new AddChatMenuEntryGui(e)));

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chatmenü")
		.description("Öffnet ein Chatmenü bei Rechtsklick auf einen Spieler im Chat.")
		.icon("player_menu")
		.subSettings(
			COPY_TEXT_ENTRY.getSetting(),
			entries
		);

	public static ChatMenu get() {
		return get(ChatMenu.class);
	}

	public void notifyChange() {
		entries.notifyChange();
	}

	@EventListener
	public void onRender(RenderTickEvent event) {
		if (activeRenderer != null)
			activeRenderer.render();
	}

	@EventListener
	public void onMouse(MouseInputEvent.Pre event) {
		if (activeRenderer != null && activeRenderer.onMouse()) {
			activeRenderer = null;
			event.cancel();
			return;
		}

		if (!Mouse.getEventButtonState())
			return;

		if (activeRenderer != null && activeRenderer.outOfBox())
			activeRenderer = null;

		if (Mouse.getEventButton() != 1 || !chatMenuBridge.isChatOpen())
			return;

		Pair<IChatComponent, IChatComponent> component = chatMenuBridge.getHoveredComponent();
		if (component == null)
			return; // Didn't click on a line

		String name = null;

		// Find name from message
		for (Pattern p : new Pattern[]{GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, CLANCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN, GLOBAL_CHAT_PATTERN}) {
			Matcher matcher = p.matcher(component.b.getFormattedText());
			if (!matcher.find())
				continue;

			name = matcher.group("name").replaceAll("§.", "");
			break;
		}

		if (name == null)
			return;

		// Create renderer
		List<ChatMenuEntry> entries = new ArrayList<>();
		if (COPY_TEXT_ENTRY.enabled)
			entries.add(COPY_TEXT_ENTRY);

		for (ChatMenuEntry entry : this.entries.get())
			if (entry.enabled)
				entries.add(entry);

		name = name.replaceAll("§.", "").trim();
		String realName = NameCache.ensureRealName(name);
		if (realName == null)
			realName = name;

		activeRenderer = new ChatMenuRenderer(entries, realName, component.a, component.b);
		event.cancel();
	}

	@EventListener
	public void onKeyboard(KeyboardInputEvent.Pre event) {
		if (activeRenderer == null)
			return;

		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == 1) {
			activeRenderer = null;
			event.cancel();
		}

		mc().dispatchKeypresses();
	}

	@Bridged
	public interface ChatMenuBridge {

		ChatMenuBridge chatMenuBridge = FileProvider.getBridge(ChatMenuBridge.class);

		boolean isChatOpen();

		Pair<IChatComponent, IChatComponent> getHoveredComponent();

	}
}