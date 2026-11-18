/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.events.MessageEvent.RawMessageModifyEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.features.uncategorized.debug.MessageDumper.CopyFormat.JSON;

class MessageDumper {

	private static final Logger LOGGER = LogManager.getLogger("GrieferUtils MessageDumper");

	private static final DropDownSetting<CopyFormat> copyFormat = DropDownSetting.create(CopyFormat.class)
		.name("Format")
		.description("Wie der gedumpte Text sein soll.")
		.defaultValue(JSON)
		.icon("command_suggestions");

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Nachrichten-Dumper")
		.description("Dumpt eingehende Nachrichten.")
		.subSettings(copyFormat)
		.icon("book_and_quill");

	@EventListener(priority = LOWEST)
	private static void onMessageModify(MessageModifyEvent event) {
		dump("gui", event.original, event.message);
	}

	@EventListener(priority = LOWEST)
	private static void onMessageModify(RawMessageModifyEvent event) {
		dump("raw", event.original, event.message);
	}

	private static void dump(String step, IChatComponent original, IChatComponent message) {
		if (enabled.get() && DebugSettings.enabled.get()) {
			Function<IChatComponent, String> toJsonString = JSON.componentToString;
			Function<IChatComponent, String> toString = copyFormat.get().componentToString;

			boolean unmodified = Objects.equals(toJsonString.apply(original), toJsonString.apply(message));
			boolean transparentlyModified = Objects.equals(toString.apply(original), toString.apply(message));

			String mod;
			if (unmodified)
				mod = "unmodified";
			else if (transparentlyModified)
				mod = "transparently modified"; // Modified, but no change in the current log format
			else
				mod = "modified to " + toString.apply(message);

			LOGGER.info("{} is {} (#{})", toString.apply(original), mod, step);
		}
	}

	enum CopyFormat implements Named {
		UNFORMATTED("Unformattiert", icc -> icc.getUnformattedText().replaceAll("§.", "")),
		FORMATTED("Formattiert", IChatComponent::getFormattedText),
		JSON("JSON", IChatComponent.Serializer::componentToJson);

		final String name;
		final Function<IChatComponent, String> componentToString;

		CopyFormat(String name, Function<IChatComponent, String> componentToString) {
			this.name = name;
			this.componentToString = componentToString;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
