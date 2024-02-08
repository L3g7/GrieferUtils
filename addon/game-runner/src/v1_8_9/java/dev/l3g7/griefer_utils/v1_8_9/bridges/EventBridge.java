/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.bridges;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.events.InputEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.InputEvent.MouseInputEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.guis.ChangelogScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class EventBridge {

	private static boolean startupComplete = false;

	/**
	 * Triggers {@link OnStartupComplete} when GuiMainMenu is opened for the first time.
	 */
	@EventListener
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (startupComplete || event.gui instanceof ChangelogScreen)
			return;

		// Call all methods annotated with @OnStartupComplete
		startupComplete = true;
		Event.fire(OnStartupComplete.class);
	}

	@EventListener
	private static void onMouseInput(MouseInputEvent e) {
		new InputEvent.MouseInputEvent(Mouse.getEventButton()).fire();
	}

	@EventListener
	private static void onMouseInput(KeyInputEvent e) {
		new InputEvent.KeyInputEvent(Keyboard.isRepeatEvent(), Keyboard.getEventKey()).fire();
	}

	@EventListener
	private static void onMouseInput(GuiScreenEvent.MouseInputEvent.Post e) {
		new InputEvent.Gui.GuiMouseInputEvent(Mouse.getEventButton()).fire();
	}

	@EventListener
	private static void onMouseInput(GuiScreenEvent.KeyboardInputEvent.Post e) {
		new InputEvent.Gui.GuiKeyInputEvent(Keyboard.isRepeatEvent(), Keyboard.getEventKey()).fire();
	}

}
