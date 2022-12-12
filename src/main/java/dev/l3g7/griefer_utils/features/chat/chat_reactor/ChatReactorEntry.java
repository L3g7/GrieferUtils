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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.labymod.utils.Material;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class ChatReactorEntry {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$(\\d+)");

	private final BooleanSetting parseAsRegEx;
	private final BooleanSetting compareEverything;
	private final StringSetting command;
	private final StringSetting name;
	private StringSetting trigger;
	private Pattern pattern = null;

	public ChatReactorEntry() {
		this(false, false, "", "", "");
	}

	private ChatReactorEntry(boolean defaultParseAsRegEx, boolean defaultCompareEverything, String defaultTrigger, String defaultName, String defaultCommand) {
		parseAsRegEx = new BooleanSetting()
			.name("Modus")
			.icon("regex")
			.custom("RegEx", "Normal")
			.description("Wie der Trigger interpretiert werden soll.")
			.defaultValue(defaultParseAsRegEx)
			.callback(isRegEx -> {
				trigger.name(isRegEx ? "Ausdruck" : "Text");
				trigger.description(isRegEx ? "Nach welchem Ausdruck überprüft werden soll." : "Auf welchen Text geachtet werden soll.");
				trigger.set(trigger.get()); // Trigger callback

				ChatReactor.saveEntries();
				ChatReactor.updateSettings();
			});

		compareEverything = new BooleanSetting()
			.name("Vergleichsmodus")
			.icon("spyglass")
			.description("Ob die gesamte Nachricht oder nur ein Teil übereinstimmen muss.")
			.custom("Alles", "Teile")
			.defaultValue(defaultCompareEverything)
			.callback(b -> {
				ChatReactor.saveEntries();
				ChatReactor.updateSettings();
			});

		trigger = new StringSetting()
			.name("Text")
			.description("Auf welchen Text geachtet werden soll.")
			.defaultValue(defaultTrigger)
			.callback(s -> {
				ChatReactor.saveEntries();
				ChatReactor.updateSettings();

				if (trigger == null)
					return;

				validateTrigger();

				if (!parseAsRegEx.get())
					return;

				trigger.name(pattern != null ? "Ausdruck" : "§cAusdruck");
				trigger.description(pattern != null ? "Nach welchem Ausdruck überprüft werden soll." : "Der Ausdrück ist ungültig.");
				ChatReactor.updateSettings();
			})
			.icon(Material.PAPER);

		command = new StringSetting()
			.name("Befehl")
			.description("Welcher Befehl ausgeführt werden soll.")
			.defaultValue(defaultCommand)
			.icon(Material.COMMAND)
			.callback(v -> {
				ChatReactor.saveEntries();
				ChatReactor.updateSettings();
			});

		name = new StringSetting()
			.name("Name")
			.description("Wie diese Reaktion hießen soll.")
			.defaultValue(defaultName)
			.icon(Material.NAME_TAG)
			.callback(v -> {
				ChatReactor.saveEntries();
				ChatReactor.updateSettings();
			});

		trigger.set(defaultTrigger); // Trigger callback
	}

	public CategorySetting getSetting() {
		return new CategorySetting()
			.name((isTriggerValid() ? "" : "§c") + name.get())
			.description(isTriggerValid() ? null : "Der Ausdrück ist ungültig.")
			.icon(parseAsRegEx.get() ? "regex" : Material.PAPER)
			.subSettings(name, parseAsRegEx, compareEverything, trigger, command);
	}

	public boolean isValid() {
		return !StringUtils.isBlank(command.get())
			&& !StringUtils.isBlank(trigger.get());
	}

	private void validateTrigger() {
		try {
			pattern = Pattern.compile(trigger.get());
		} catch (Exception e) {
			pattern = null;
		}
	}

	private boolean isTriggerValid() {
		return isValid() && (!parseAsRegEx.get() || pattern != null);
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("is_regex", parseAsRegEx.get());
		object.addProperty("compare_everything", compareEverything.get());
		object.addProperty("name", name.get());
		object.addProperty("trigger", trigger.get());
		object.addProperty("command", command.get());

		return object;
	}

	public static ChatReactorEntry fromJson(JsonObject object) {
		try {
			return new ChatReactorEntry(
				object.get("is_regex").getAsBoolean(),
				object.get("compare_everything").getAsBoolean(),
				object.get("trigger").getAsString(),
				object.get("name").getAsString(),
				object.get("command").getAsString()
			);
		} catch (Throwable t) { // Make sure MC doesn't crash when the entry is incomplete (compare_everything was added in 1.9)
			t.printStackTrace();
			return new ChatReactorEntry();
		}
	}

	public void checkMatch(String text) {
		if (!isTriggerValid())
			return;

		if (!parseAsRegEx.get()) {
			if (compareEverything.get() ? trigger.get().equals(text) : text.contains(trigger.get()))
				player().sendChatMessage(command.get());
			return;
		}

		Matcher matcher = Pattern.compile(trigger.get()).matcher(text);

		if (!(compareEverything.get() ? matcher.matches() : matcher.find()))
			return;

		String command = this.command.get();

		Matcher replaceMatcher = PLACEHOLDER_PATTERN.matcher(command);

		try {
			while (replaceMatcher.find())
				command = command.replaceFirst(PLACEHOLDER_PATTERN.pattern(), matcher.group(Integer.parseInt(replaceMatcher.group(1))));
		} catch (Exception e) {
			display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + this.command.get() + "\" existiert nicht in \"" + trigger.get() + "\"");
			return;
		}

		command = command.replace('§', '&');


		player().sendChatMessage(command);
	}
}