/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.misc.functions.Function;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.minecraft.init.Items;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;

public class CopyTextEntry extends ChatMenuEntry {

	protected final String configKey = "chat.chat_menu.entries." + name + ".";

	private final DropDownSetting<CopyFormat> copyFormat = DropDownSetting.create(CopyFormat.class)
		.name("Format")
		.description("Wie der kopierte Text sein soll.")
		.config(configKey + "format")
		.defaultValue(CopyFormat.UNFORMATTED)
		.icon(Items.paper);

	private final SwitchSetting modifiedMessage = SwitchSetting.create()
		.name("Bearbeitungen kopieren")
		.description("Ob der Text mit den Bearbeitungen u.a. von GrieferUtils kopiert werden soll.")
		.config(configKey + "modified_message")
		.icon(Items.writable_book);

	private final SwitchSetting mainSetting = LabyBridge.get(SwitchSetting::create /* TODO: LM3 DisplaySetting */, LM4DisplaySetting::new)
		.name(name)
		.icon(icon)
		.defaultValue(true)
		.config(configKey + "enabled")
		.callback(v -> enabled = v)
		.subSettings(copyFormat, modifiedMessage);

	public CopyTextEntry() {
		super("Text kopieren", null, null, "clipboard");
	}

	public void trigger(IChatComponent modifiedComponent, IChatComponent originalComponent) {
		IChatComponent icc = modifiedMessage.get() ? modifiedComponent : originalComponent;
		ChatMenu.copyToClipboard(copyFormat.get().componentToString.apply(icc));
	}

	public BaseSetting<?> getSetting() {
		return mainSetting;
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

	@ExclusiveTo(LABY_4)
	private static class LM4DisplaySetting extends SwitchSettingImpl {

		public LM4DisplaySetting() {
			EventRegisterer.register(this);
		}

		/**
		 * Replaces the advanced-button icon with pencil_vec.
		 */
		@EventListener
		private void onInit(SettingActivityInitEvent event) {
			if (event.holder() != parent)
				return;

			for (Widget w : event.settings().getChildren()) {
				if (w instanceof SettingWidget s && s.setting() == this) {
					SettingsImpl.hookChildAdd(s, e -> {
						if (e.childWidget() instanceof FlexibleContentWidget content) {
							ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
							btn.updateIcon(SettingsImpl.buildIcon("pencil_vec"));
						}
					});
					break;
				}
			}

		}

	}

}
