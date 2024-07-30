/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_reactor.laby4;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public class ChatReaction {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\$|\\$\\d+)");
	private static final Pattern AND_COLOR_PATTERN = Pattern.compile("(?i)(?<!\\\\)&[\\da-fk-or]");
	private static final Pattern PARAGRAPH_COLOR_PATTERN = Pattern.compile("(?i)(?<!\\\\)(?:§|\\\\u00A7)[\\da-fk-or]");

	boolean enabled;
	Boolean regEx;
	boolean matchAll;
	String trigger = "";
	String command = "";
	Citybuild citybuild = Citybuild.ANY;
	boolean completed;

	public ChatReaction() {}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("enabled", enabled);
		object.addProperty("is_regex", regEx);
		object.addProperty("match_all", matchAll);
		object.addProperty("trigger", trigger);
		object.addProperty("command", command);
		object.addProperty("city_build", citybuild.getInternalName());

		return object;
	}

	public static ChatReaction fromJson(JsonObject object) {
		ChatReaction reaction = new ChatReaction();
		reaction.enabled = object.get("enabled").getAsBoolean();
		reaction.regEx = object.get("is_regex").getAsBoolean();
		reaction.matchAll = object.get("match_all").getAsBoolean();
		reaction.trigger = object.get("trigger").getAsString();
		reaction.command = object.get("command").getAsString();
		reaction.citybuild = Citybuild.getCitybuild(object.get("city_build").getAsString());
		reaction.completed = true;
		return reaction;
	}

	public void processMessage(String text) {
		if (!completed || !enabled)
			return;

		String trigger = this.trigger;

		if (PARAGRAPH_COLOR_PATTERN.matcher(trigger).find())
			trigger = trigger.replace(PARAGRAPH_COLOR_PATTERN.pattern(), "&$1");

		boolean triggerHasColor = AND_COLOR_PATTERN.matcher(trigger).find();
		text = text.replaceAll("(?i)§([\\da-fk-or])", triggerHasColor ? "&$1" : "");

		String command = this.command;
		if (!regEx) {
			if (matchAll ? trigger.equalsIgnoreCase(text) : text.toLowerCase().contains(trigger.toLowerCase()) && !MessageSendEvent.post(command))
				player().sendChatMessage(command);
			return;
		}

		Matcher matcher = Pattern.compile(trigger).matcher(text);

		if (!matcher.find())
			return;

		Matcher replaceMatcher = PLACEHOLDER_PATTERN.matcher(command);
		while (replaceMatcher.find()) {
			String group = replaceMatcher.group(1).substring(1);
			String replacement = group.equals("$") ? "\\$" : matcher.group(Integer.parseInt(group));
			command = command.replaceFirst("\\$" + Pattern.quote(group), replacement.replaceAll("(?i)[&§]([\\da-fk-or])", ""));
		}

		if (!MessageSendEvent.post(command))
			player().sendChatMessage(command);
	}

}