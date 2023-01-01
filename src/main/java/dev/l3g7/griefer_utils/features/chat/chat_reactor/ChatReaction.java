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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.misc.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class ChatReaction {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\\\(\\d+)");
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?<!\\\\)[§&][\\da-fk-or]");

	Boolean regEx;
	boolean matchAll;
	String trigger = "";
	String command = "";
	boolean completed;
	int pos = -1;

	public ChatReaction() {}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("is_regex", regEx);
		object.addProperty("compare_everything", matchAll);
		object.addProperty("trigger", trigger);
		object.addProperty("command", command);

		return object;
	}

	public static ChatReaction fromJson(JsonObject object) {
		ChatReaction reaction = new ChatReaction();
		reaction.regEx = object.get("is_regex").getAsBoolean();
		reaction.matchAll = object.get("compare_everything").getAsBoolean();
		reaction.trigger = object.get("trigger").getAsString();
		reaction.command = object.get("command").getAsString();
		reaction.completed = true;
		return reaction;
	}

	public void processMessage(String text) {
		if (!completed)
			return;

		// Remove color if trigger doesn't have any
		if (!COLOR_PATTERN.matcher(trigger).find())
			text = text.replaceAll(COLOR_PATTERN.pattern(), "");

		if (!regEx) {
			if (matchAll ? trigger.equalsIgnoreCase(text) : text.toLowerCase().contains(trigger.toLowerCase()))
				player().sendChatMessage(command);
			return;
		}

		Matcher matcher = Pattern.compile(trigger).matcher(text);

		if (!matcher.find())
			return;

		Matcher replaceMatcher = PLACEHOLDER_PATTERN.matcher(command);

		try {
			while (replaceMatcher.find())
				command = command.replaceFirst(PLACEHOLDER_PATTERN.pattern(), matcher.group(Integer.parseInt(replaceMatcher.group(1))));
		} catch (Exception e) {
			display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + command + "\" existiert nicht in \"" + trigger + "\"");
			return;
		}

		command = command.replace('§', '&');


		player().sendChatMessage(command);
	}
}