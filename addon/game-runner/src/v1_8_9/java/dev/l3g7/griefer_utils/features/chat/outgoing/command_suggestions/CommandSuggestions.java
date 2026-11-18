/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.Node;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.Suggestion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class CommandSuggestions extends Feature {

	private static final Gson gson = new GsonBuilder()
		.registerTypeAdapter(Node.class, Node.DESERIALIZER)
		.registerTypeAdapter(ArgumentNode.class, ArgumentNode.DESERIALIZER)
		.registerTypeAdapter(Requirement.class, Requirement.DESERIALIZER)
		.registerTypeAdapter(Suggestion.class, Suggestion.DESERIALIZER)
		.create();

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Befehlsvorschläge")
		.description("Zeigt Vorschläge beim Schreiben von Befehlen an.")
		.icon("command_suggestions")
		.since("2.4-BETA-1");

	private static CommandDispatcher dispatcher = null;
	private static final Logger logger = LogManager.getLogger("CommandSuggestions");

	public static CommandDispatcher getDispatcher() {
		if (!get(CommandSuggestions.class).isEnabled() || !ServerCheck.isOnGrieferGames())
			return null;

		return dispatcher;
	}

	@EventListener
	private static void onStaticData(StaticDataReceiveEvent event) {
		dispatcher = new CommandDispatcher();
		Node<?>[] nodes = event.read("commands", json -> gson.fromJson(json, Node[].class));
		for (Node<?> node : nodes)
			node.register(dispatcher, dispatcher.getRoot()::addChild);

		if (dispatcher.getErrors() > 0)
			logger.warn("Dropped {} commands", new Object[]{dispatcher.getErrors()});
	}

}
