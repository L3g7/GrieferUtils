/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing;

import de.emotechat.addon.gui.chat.suggestion.EmoteSuggestionsMenu;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.KeyboardInputEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.ingamechat.GuiChatCustom;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason.NOT_NEEDED;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.send;
import static org.lwjgl.input.Keyboard.*;

@Singleton
public class SplitLongMessages extends Feature {

	private static final List<String> lastParts = new ArrayList<>();
	private static String lastRecipient = null;
	private static int previousLength = 100;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lange Nachrichten aufteilen")
		.description("Teilt Nachrichten, die das Zeichenlimit überschreiten, in mehrere Nachrichten auf.\n" +
			"Funktioniert im öffentlichen Chat sowie mit /msg und /r.")
		.icon("shears");

	public static SplitLongMessages get() {
		return get(SplitLongMessages.class);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiKeyboardInput(KeyboardInputEvent.Post event) {
		if (!(event.gui instanceof GuiChat))
			return;

		GuiTextField inputField = Reflection.get(event.gui, "inputField");
		if (!enabled.get()) {
			inputField.setMaxStringLength(previousLength);
			return;
		}

		if (inputField.getText().length() <= 100 && (getEventKey() == KEY_UP || getEventKey() == KEY_DOWN)) {
			Reflection.set(inputField, "lineScrollOffset", 0);
			inputField.setCursorPositionEnd();
		}

		if (LABY_3.isActive()) {
			int width = event.gui.width - 4;
			if (event.gui instanceof GuiChatCustom) {
				Object[] chatButtons = Reflection.get(event.gui, "chatButtons");
				if (chatButtons != null)
					width -= chatButtons.length * 14;
			}

			Reflection.set(inputField, "width", width);
		}

		String text = inputField.getText().toLowerCase();
		if (!(text.startsWith("/msg ") || text.startsWith("/r ") || !(text.startsWith("/")))) { // NOTE: refactor
			inputField.setMaxStringLength(previousLength);
			return;
		}

		if (inputField.getMaxStringLength() != Integer.MAX_VALUE)
			previousLength = inputField.getMaxStringLength();
		inputField.setMaxStringLength(Integer.MAX_VALUE);
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (!lastParts.isEmpty() && cancelSending(event.message.getUnformattedText())) {
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
		if (msg.equals("Fehler: Spieler nicht gefunden.")
			|| msg.equals("Bitte schreibe keine IP-Adressen oder Webseiten in den Chat."))
			return true;

		Matcher matcher = BLACKLIST_ERROR_PATTERN.matcher(msg);
		return matcher.matches() && matcher.group("player").replaceAll("§.", "").equals(MinecraftUtil.name());
	}

	@EventListener
	public void onSend(MessageEvent.MessageAboutToBeSentEvent event) {
		try {
			onSendCaught(event);
		} catch (Throwable t) {
			BugReporter.reportError(Util.elevate(t, "Tried to process \"" + event.message + "\""));
		}
	}

	private void onSendCaught(MessageEvent.MessageAboutToBeSentEvent event) {
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
					text = new StringBuilder(getLastColor(text.toString()));
				}

				int index = length;
				if (length(text) > 0)
					index = Math.max(0, index - length(text));

				String part = word.substring(0, index);
				if (part.endsWith("&"))
					part = word.substring(0, --index);

				word = word.substring(index);

				if (length(text) > 0)
					text.append(" ");

				text.append(part);
			}

			if (length(text) + word.length() > length) {
				messages.add(text.toString());
				text = new StringBuilder(getLastColor(text.toString()));
			}

			if (length(text) > 0)
				text.append(" ");

			text.append(word);
		}

		if (length(text) > 0)
			messages.add(text.toString());

		return messages;
	}

	private static String getLastColor(String text) {
		int lastIndex = text.length();

		while (true) {
			lastIndex = text.lastIndexOf('&', lastIndex - 1);
			if (lastIndex == -1)
				return "";

			if (lastIndex == text.length() - 1)
				continue;

			char colorChar = Character.toLowerCase(text.charAt(lastIndex + 1));
			if (colorChar == 'r')
				return "";

			if (colorChar >= '0' && colorChar <= '9'
				|| colorChar >= 'a' && colorChar <= 'f')
				return "&" + colorChar;
		}
	}

	private static int length(StringBuilder stringBuilder) {
		if (stringBuilder.length() == 0)
			return 0;

		return stringBuilder.length() + 1;
	}

	@ExclusiveTo(value = LABY_3, reason = NOT_NEEDED)
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

			if (SplitLongMessages.get().isEnabled() && text.startsWith("/msg ") || text.startsWith("/r ") || !text.startsWith("/"))
				minecraftTextFieldLength = Integer.MAX_VALUE;
			else
				minecraftTextFieldLength = textFieldLength;
		}

		@Inject(method = "adjustTextFieldLength", at = @At("TAIL"), remap = false)
		public void injectAdjustTextFieldLengthTail(CallbackInfo ci) {
			minecraftTextFieldLength = textFieldLength;
		}

	}

}
