/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class ActionBar {

	private static String currentText;
	private static IChatComponent originalMessage = null;
	private static long originalDisplayEnd = -1;

	public static void set(String text) {
		if (text == null && currentText != null) {
			mc().ingameGUI.setRecordPlaying("", false);

			if (originalDisplayEnd > System.currentTimeMillis()) {
				mc().ingameGUI.setRecordPlaying(originalMessage, false);
				int time = (int) ((originalDisplayEnd - System.currentTimeMillis()) / 50);
				Reflection.set(mc().ingameGUI, "recordPlayingUpFor", time);
			}
		}

		currentText = text;
	}

	@EventListener
	private static void onTickEvent(TickEvent.ClientTickEvent event) {
		if (currentText != null)
			mc().ingameGUI.setRecordPlaying(currentText, false);
	}

	@EventListener
	private static void onActionbar(MessageReceiveEvent event) {
		if (event.type == 2 && currentText != null) {
			event.cancel();
			originalDisplayEnd = System.currentTimeMillis() + 3_000;
			originalMessage = event.message;
		}
	}

}
