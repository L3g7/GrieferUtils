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
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.misc.Citybuild;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class ChatReaction {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\\\(\\d+)");
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?<!\\\\)[§&][\\da-fk-or]");

	boolean enabled;
	Boolean regEx;
	boolean matchAll;
	String trigger = "";
	String command = "";
	Citybuild cityBuild = Citybuild.ANY;
	boolean completed;

	public ChatReaction() {}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("enabled", enabled);
		object.addProperty("is_regex", regEx);
		object.addProperty("match_all", matchAll);
		object.addProperty("trigger", trigger);
		object.addProperty("command", command);
		object.addProperty("city_build", cityBuild.getInternalName());

		return object;
	}

	public static ChatReaction fromJson(JsonObject object) {
		ChatReaction reaction = new ChatReaction();
		reaction.enabled = object.get("enabled").getAsBoolean();
		reaction.regEx = object.get("is_regex").getAsBoolean();
		reaction.matchAll = object.get("match_all").getAsBoolean();
		reaction.trigger = object.get("trigger").getAsString();
		reaction.command = object.get("command").getAsString();
		reaction.cityBuild = Citybuild.getCitybuild(object.get("city_build").getAsString());
		reaction.completed = true;
		return reaction;
	}

	public void processMessage(String text) {
		if (!completed || !enabled)
			return;

		// Remove color if trigger doesn't have any
		if (!COLOR_PATTERN.matcher(trigger).find())
			text = text.replaceAll(COLOR_PATTERN.pattern(), "");

		String command = this.command;
		if (!regEx) {
			if (matchAll ? trigger.equalsIgnoreCase(text) : text.toLowerCase().contains(trigger.toLowerCase()) && !MessageEvent.MessageSendEvent.post(command))
				player().sendChatMessage(command);
			return;
		}

		Matcher matcher = Pattern.compile(trigger).matcher(text);

		if (!matcher.find())
			return;

		Matcher replaceMatcher = PLACEHOLDER_PATTERN.matcher(command);

		while (replaceMatcher.find())
			command = command.replaceFirst(PLACEHOLDER_PATTERN.pattern(), matcher.group(Integer.parseInt(replaceMatcher.group(1))));

		command = command.replace('§', '&');
		if (!MessageEvent.MessageSendEvent.post(command))
			player().sendChatMessage(command);
	}

}