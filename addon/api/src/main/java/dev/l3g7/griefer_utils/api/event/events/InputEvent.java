/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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
