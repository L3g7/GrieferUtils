/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class ChatDeduplicator extends Feature {

	private final Map<String, MessageState> states = new HashMap<>();

	private final SwitchSetting stackMessages = SwitchSetting.create()
		.name("Nachrichten stapeln")
		.description("Zeigt Duplikate als gestapelte Version an (z.B. 'Hey (2)') statt sie auszublenden.")
		.icon("stack")
		.defaultValue(true);

	private final SliderSetting resetInterval = SliderSetting.create()
		.name("Reset-Intervall (Minuten)")
		.description("Nach wie vielen Minuten ohne Duplikat der Zähler zurückgesetzt wird.\n0 = nie zurücksetzen.")
		.icon("clock")
		.min(0).max(60)
		.defaultValue(5);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chat-Duplikat-Filter")
		.description("Erkennt automatisch doppelte Nachrichten desselben Spielers und stapelt oder blendet sie aus.")
		.icon("crossed_out_chat")
		.since("2.5.0")
		.subSettings(stackMessages, resetInterval);

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		String formatted = event.message.getFormattedText();
		if (Constants.MESSAGE_PATTERNS.stream().noneMatch(p -> p.matcher(formatted).matches()))
			return;

		String key = formatted
			.replaceAll("§.", "");

		MessageState state = states.get(key);
		long now = System.currentTimeMillis();
		long intervalMs = (long) resetInterval.get() * 60 * 1000;
		boolean expired = state == null || (intervalMs > 0 && now - state.lastSeen >= intervalMs);

		if (expired) {
			if (state != null)
				states.remove(key);
			if (stackMessages.get()) {
				int id = key.hashCode() & 0x7fffffff;
				if (id == 0) id = 1;
				states.put(key, new MessageState(id, event.message, now));
				event.cancel();
				mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(event.message, id);
			} else {
				states.put(key, new MessageState(0, event.message, now));
			}
		} else {
			state.count++;
			state.lastSeen = now;
			event.cancel();

			if (stackMessages.get()) {
				IChatComponent stacked = state.originalComponent.createCopy();
				stacked.appendSibling(new ChatComponentText(" §7(" + state.count + ")"));
				mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(stacked, state.chatLineId);
			}
		}
	}

	private static class MessageState {
		int count = 1;
		long lastSeen;
		int chatLineId;
		IChatComponent originalComponent;

		MessageState(int chatLineId, IChatComponent originalComponent, long lastSeen) {
			this.chatLineId = chatLineId;
			this.originalComponent = originalComponent;
			this.lastSeen = lastSeen;
		}
	}

}
