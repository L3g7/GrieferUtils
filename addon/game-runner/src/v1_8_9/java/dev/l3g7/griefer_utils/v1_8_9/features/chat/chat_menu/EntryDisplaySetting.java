/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.laby4.settings.types.SwitchSettingImpl;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@ExclusiveTo(LABY_4) // TODO LM3 EntryDisplaySetting
public class EntryDisplaySetting extends SwitchSettingImpl {

	public final ChatMenuEntry entry;

	public EntryDisplaySetting(ChatMenuEntry entry) {
		this.entry = entry;
		EventRegisterer.register(this);
		initEntry();
		callback(enabled -> {
			entry.enabled = enabled;
			ChatMenu.saveEntries();
		});
	}

	public void initEntry() {
		name(entry.name + "\n§o➡ " + entry.command);
		set(entry.enabled);

		switch (entry.iconType) {
			case SYSTEM:
				icon(entry.icon);
				break;
			case DEFAULT:
				icon(entry.action.defaultIcon);
				break;
			case IMAGE_FILE:
				icon(new ResourceLocation("griefer_utils/user_content/" + entry.icon.hashCode()));
				break;
			case ITEM:
				icon(entry.getIconAsItemStack());
		}
	}

	public void delete() {
		parent.unregister(kv -> kv.getValue() == this);
		ChatMenu.saveEntries();
	}

	@Override
	public boolean hasAdvancedButton() {
		return true;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != parent)
			return;

		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() == this) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						ButtonWidget btn = ButtonWidget.icon(SettingsImpl.buildIcon("pencil_vec"), () ->
							mc().displayGuiScreen(new AddChatMenuEntryGui(this, mc().currentScreen)));

						btn.addId("advanced-button"); // required so LSS is applied
						content.removeChild("advanced-button");
						content.addContent(btn);
					}
				});
				break;
			}
		}

	}

}