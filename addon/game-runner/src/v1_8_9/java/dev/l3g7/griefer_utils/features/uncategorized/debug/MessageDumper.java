/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.init.Items;
import net.minecraft.util.IChatComponent;

class MessageDumper {

	private static final DropDownSetting<CopyFormat> copyFormat = DropDownSetting.create(CopyFormat.class)
		.name("Format")
		.description("Wie der gedumpte Text sein soll.")
		.defaultValue(CopyFormat.JSON)
		.icon("XZRF:command_suggestions");

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Nachrichten-Dumper")
		.description("Dumpt eingehende Nachrichten.")
		.subSettings(copyFormat)
		.icon("XZRF:book_and_quill");

	@EventListener(priority = Priority.LOWEST)
	private static void onMessageModify(MessageModifyEvent event) {
		if (enabled.get() && DebugSettings.enabled.get()) {
			Function<IChatComponent, String> toString = copyFormat.get().componentToString;
			System.out.println(toString.apply(event.original) + " was modified to " + toString.apply(event.message));
		}
	}

	private enum CopyFormat implements Named {
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
