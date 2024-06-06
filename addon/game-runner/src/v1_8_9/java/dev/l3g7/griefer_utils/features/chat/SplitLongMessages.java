/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.labymod.api.Laby;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.ChatInputOverlay;
import net.labymod.ingamechat.GuiChatCustom;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.send;
import static org.lwjgl.input.Keyboard.*;

@Singleton
public class SplitLongMessages extends Feature {

	private static final List<String> lastParts = new ArrayList<>();
	private static String lastRecipient = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lange Nachrichten aufteilen")
		.description("Teilt Nachrichten, die das Zeichenlimit überschreiten, in mehrere Nachrichten auf.\n" +
			"Funktioniert im öffentlichen Chat sowie mit /msg und /r.")
		.icon(Items.shears);

	@EventListener
	public void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!(event.gui instanceof GuiChat))
			return;

		GuiTextField inputField = Reflection.get(event.gui, "inputField");
		if (inputField.getText().length() <= 100 && (getEventKey() == KEY_UP || getEventKey() == KEY_DOWN)) {
			Reflection.set(inputField, "lineScrollOffset", 0);
			inputField.setCursorPositionEnd();
		}

		int width = 626;
		if (LABY_4.isActive()) { // NOTE: replace with switch
			if (Laby.labyAPI().minecraft().minecraftWindow().currentLabyScreen() instanceof ChatInputOverlay)
				width -= (int) Laby.labyAPI().chatProvider().chatInputService().getButtonWidth();
		} else {
			if (event.gui instanceof GuiChatCustom) {
				Object[] chatButtons = Reflection.get(event.gui, "chatButtons");
				width -= chatButtons.length * 14;
			}
		}

		Reflection.set(inputField, "width", width); // Only accessible in Forge

		String text = inputField.getText().toLowerCase();
		if (!(text.startsWith("/msg ") || text.startsWith("/r ") || !(text.startsWith("/")))) { // NOTE: refactor
			inputField.setMaxStringLength(100);
			return;
		}

		inputField.setMaxStringLength(Integer.MAX_VALUE);
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (!lastParts.isEmpty() && cancelSending(event.message.getFormattedText())) {
			lastParts.forEach(ChatQueue::remove);
			lastParts.clear();
			return;
		}

		for (Pattern pattern : new Pattern[] {MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN}) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				continue;

			lastRecipient = matcher.group("name").replaceAll("§.", "");
			return;
		}
	}

	private boolean cancelSending(String msg) {
		if (msg.equals("§r§cFehler:§r§4 §r§4Spieler nicht gefunden.§r")
			|| msg.equals("§r§7Bitte schreibe keine IP-Adressen oder Webseiten in den Chat.§r"))
			return true;

		Matcher matcher = BLACKLIST_ERROR_PATTERN.matcher(msg);
		return matcher.matches() && matcher.group("player").replaceAll("§.", "").equals(MinecraftUtil.name());
	}

	@EventListener
	public void onSend(MessageEvent.MessageAboutToBeSentEvent event) {
		String text = event.message;

		if (!lastParts.isEmpty() && text.equals(lastParts.get(lastParts.size() - 1))) {
			lastParts.clear();
			return;
		}

		if (text.length() <= 100)
			return;

		if (text.startsWith("/r ") && lastRecipient != null)
			text = String.format("/msg %s %s", lastRecipient, text.substring(3));

		int index = text.toLowerCase().startsWith("/msg ") ? text.indexOf(' ') + 1 : 0;
		index = text.toLowerCase().startsWith("/") ? text.indexOf(' ', index) + 1 : 0;
		if (text.startsWith("@"))
			index = 1;

		String message = text.substring(index);
		String prefix = text.substring(0, index);

		for (String s : cutUp(message, 100 - prefix.length())) {
			if (text.startsWith("@"))
				ChatQueue.queuedSlowMessages.add(prefix + s);
			else
				send(prefix + s);
			lastParts.add(prefix + s);
		}
		event.cancel();
	}

	private static List<String> cutUp(String string, int length) {
		if (length == 0)
			throw new IllegalArgumentException("Required text length is 0!");

		List<String> messages = new ArrayList<>();
		String[] words = string.split(" ");
		StringBuilder text = new StringBuilder();

		for (String word : words) {
			while (word.length() > length) {
				if (length(text) >= length) {
					messages.add(text.toString());
					text = new StringBuilder();
				}

				int index = length;
				if (length(text) > 0)
					index = Math.max(0, index - length(text));

				String part = word.substring(0, index);
				word = word.substring(index);

				if (length(text) > 0)
					text.append(" ");

				text.append(part);
			}

			if (length(text) + word.length() > length) {
				messages.add(text.toString());
				text = new StringBuilder();
			}

			if (length(text) > 0)
				text.append(" ");

			text.append(word);
		}

		if (length(text) > 0)
			messages.add(text.toString());

		return messages;
	}

	private static int length(StringBuilder stringBuilder) {
		if (stringBuilder.length() == 0)
			return 0;

		return stringBuilder.length() + 1;
	}
/*
	@Mixin(value = EmoteSuggestionsMenu.class, remap = false)
	private static class MixinEmoteSuggestionsMenu {

		@Shadow
		private GuiTextField textField;
		@Shadow
		private int minecraftTextFieldLength;

		private int textFieldLength;

		@Inject(method = "adjustTextFieldLength", at = @At("HEAD"), remap = false)
		public void injectAdjustTextFieldLength(CallbackInfo ci) {
			textFieldLength = minecraftTextFieldLength;
			String text = textField.getText();

			if (FileProvider.getSingleton(SplitLongMessages.class).isEnabled() && text.startsWith("/msg ") || text.startsWith("/r ") || !text.startsWith("/"))
				minecraftTextFieldLength = Integer.MAX_VALUE;
			else
				minecraftTextFieldLength = textFieldLength;
		}

		@Inject(method = "adjustTextFieldLength", at = @At("TAIL"), remap = false)
		public void injectAdjustTextFieldLengthTail(CallbackInfo ci) {
			minecraftTextFieldLength = textFieldLength;
		}

	}
*/
}
