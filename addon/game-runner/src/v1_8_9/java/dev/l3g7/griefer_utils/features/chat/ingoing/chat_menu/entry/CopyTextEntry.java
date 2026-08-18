/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry;

import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.features.uncategorized.commands.Commands.CommandBridge.commandBridge;

public class CopyTextEntry extends ChatMenuEntry {

	protected final String configKey = "chat.ingoing.chat_menu.entries." + name + ".";

	public final DropDownSetting<CopyFormat> copyFormat = DropDownSetting.create(CopyFormat.class)
		.name("Format")
		.description("Wie der kopierte Text sein soll.")
		.config(configKey + "format")
		.defaultValue(CopyFormat.UNFORMATTED)
		.icon("command_suggestions");

	public final SwitchSetting modifiedMessage = SwitchSetting.create()
		.name("Bearbeitungen kopieren")
		.description("Ob der Text mit den Bearbeitungen u.a. von GrieferUtils kopiert werden soll.")
		.config(configKey + "modified_message")
		.icon("book_and_quill");

	private final SwitchSetting mainSetting = ChatMenu.ChatMenuBridge.chatMenuVersioned.createCopyEntry(this)
		.name(name)
		.defaultValue(true)
		.config(configKey + "enabled")
		.callback(v -> enabled = v);

	public CopyTextEntry() {
		super("Text kopieren", null, null, "book_and_quill");
	}

	@Override
	public void trigger(String name, IChatComponent modifiedComponent, IChatComponent originalComponent) {
		IChatComponent icc = modifiedMessage.get() ? modifiedComponent : originalComponent;
		commandBridge.copy(copyFormat.get().componentToString.apply(icc));
	}

	public BaseSetting<?> getSetting() {
		return mainSetting;
	}

	public enum CopyFormat implements Named {
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
