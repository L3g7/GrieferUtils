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

package dev.l3g7.griefer_utils.features.chat;

import de.emotechat.addon.gui.chat.suggestion.EmoteSuggestionsMenu;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ChatQueue;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.utils.Material;
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

import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;

@Singleton
public class SplitLongMessages extends Feature {

	private static final List<String> lastParts = new ArrayList<>();
	private static String lastRecipient = null;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Lange Nachrichten aufteilen")
		.description("Teilt Nachrichten, die das Zeichenlimit überschreiten, in mehrere Nachrichten auf.\n" +
			"Funktioniert im öffentlichen Chat sowie mit /msg und /r.")
		.icon(Material.SHEARS);

	@EventListener
	public void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!(event.gui instanceof GuiChat))
			return;

		GuiTextField inputField = Reflection.get(event.gui, "inputField");
		Reflection.set(inputField, 626, "width"); // Only accessible in Forge
		if (event.gui instanceof GuiChatCustom) {
			Object[] chatButtons = Reflection.get(event.gui, "chatButtons");
			Reflection.set(inputField, inputField.getWidth() - chatButtons.length * 14, "width");
		}

		String text = inputField.getText().toLowerCase();
		if (!(text.startsWith("/msg ") || text.startsWith("/r ") || !(text.startsWith("/") || text.startsWith("@")))) {
			inputField.setMaxStringLength(100);
			return;
		}

		inputField.setMaxStringLength(Integer.MAX_VALUE);
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (!lastParts.isEmpty() && event.message.getUnformattedText().equals("Fehler: Spieler nicht gefunden.")) {
			lastParts.forEach(ChatQueue::remove);
			return;
		}

		for (Pattern pattern : new Pattern[] {Constants.MESSAGE_RECEIVE_PATTERN, Constants.MESSAGE_SEND_PATTERN}) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				continue;

			lastRecipient = matcher.group("name").replaceAll("§.", "");
			return;
		}
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

		String message = text.substring(index);
		String prefix = text.substring(0, index);

		for (String s : cutUp(message, 100 - prefix.length())) {
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


}
