/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.network.play.server.S3EPacketTeams;

import java.util.HashMap;
import java.util.Map;

/**
 * Fired when the value of an element in the scoreboard changes.
 */
public class ScoreboardUpdateEvent extends Event {

	private static final Map<String, ScoreboardElement> ELEMENTS = new HashMap<>();
	public final String key;
	public final String value;

	public ScoreboardUpdateEvent(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@EventListener
	private static void onTeams(PacketEvent.PacketReceiveEvent<S3EPacketTeams> event) {
		String teamName = event.packet.getName();
		int lastUnderscore = teamName.lastIndexOf('_');
		if (lastUnderscore == -1)
			return;

		String id = teamName.substring(0, lastUnderscore);
		String type = teamName.substring(lastUnderscore + 1);

		String rowValue = (event.packet.getPrefix() + event.packet.getSuffix()).replaceAll("§.", "");
		if (rowValue.isEmpty())
			return;

		ScoreboardElement element = ELEMENTS.computeIfAbsent(id, _id -> new ScoreboardElement(null, null));

		if (type.equals("title")) {
			element.key = rowValue.substring(2);
		} else if (type.equals("value")) {
			if (element.key != null && !rowValue.equals(element.value))
				new ScoreboardUpdateEvent(element.key, rowValue).fire();

			element.value = rowValue;
		}
	}

	private static class ScoreboardElement {
		public String key, value;

		public ScoreboardElement(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

}
