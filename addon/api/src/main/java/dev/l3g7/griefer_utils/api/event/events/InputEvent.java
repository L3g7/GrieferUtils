/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.event.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;

public class InputEvent extends Event {

	public static class MouseInputEvent extends InputEvent {

		public final int button;

		public MouseInputEvent(int button) {
			this.button = button;
		}

	}
	public static class KeyInputEvent extends InputEvent {

		public final boolean isRepeat;
		public final int keyCode;

		public KeyInputEvent(boolean isRepeat, int keyCode) {
			this.isRepeat = isRepeat;
			this.keyCode = keyCode;
		}

	}

	public static class Gui {

		public static class GuiMouseInputEvent extends MouseInputEvent {

			public GuiMouseInputEvent(int button) {
				super(button);
			}

		}

		public static class GuiKeyInputEvent extends KeyInputEvent {

			public GuiKeyInputEvent(boolean isRepeat, int keyCode) {
				super(isRepeat, keyCode);
			}

		}

	}

}
