/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

public class _ChatMenuEntry {

	String name = "";
	Action action;
	String command = "";
	IconType iconType = null;
	Object icon = null;
	boolean completed = false;

	enum Action {
		OPEN_URL("Url öffnen", "earth_grid"),
		RUN_CMD("Befehl ausführen", "cpu"),
		SUGGEST_CMD("Befehl vorschlagen", "speech_bubble");

		public final String name;
		public final String defaultIcon;
		Action(String name, String defaultIcon) {
			this.name = name;
			this.defaultIcon = defaultIcon;
		}
	}

	enum IconType {
		DEFAULT("Standard", null),
		ITEM("Item", "gold_ingot"),
		IMAGE("Bild", "tree_file");

		public final String name;
		public final String defaultIcon;
		IconType(String name, String defaultIcon) {
			this.name = name;
			this.defaultIcon = defaultIcon;
		}
	}
}