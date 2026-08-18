/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu.ChatMenuBridge;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.CopyTextEntry;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.EntryDisplaySetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.ChatInputOverlay;
import net.minecraft.util.IChatComponent;

import java.util.Iterator;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ChatMenuLaby4 implements ChatMenuBridge {

	@Override
	public EntryDisplaySetting createEntry(ChatMenuEntry entry) {
		return new EntryDisplaySettingLaby4(entry);
	}

	@Override
	public SwitchSetting createCopyEntry(CopyTextEntry target) {
		return new CopyDisplaySettingLaby4()
			.subSettings(target.copyFormat, target.modifiedMessage);
	}

	@Override
	public boolean isChatOpen() {
		return Laby4Util.getActivity() instanceof ChatInputOverlay;
	}

	@Override
	public Pair<IChatComponent, IChatComponent> getHoveredComponent() {
		return ChatLineUtil.getHoveredComponent();
	}

	/**
	 * Ensures the EntryAddSetting is always the last child.
	 */
	@EventListener
	public static void onInit(SettingActivityInitEvent event) {
		if (event.holder() != ChatMenu.get().getMainElement())
			return;

		Iterator<Widget> it = event.settings().getChildren().iterator();
		SettingWidget widget = null;
		while (it.hasNext()) {
			Widget w = it.next();
			if (w instanceof SettingWidget s && s.setting() == ChatMenu.get().getAddSetting()) {
				widget = s;
				it.remove();
				break;
			}
		}

		event.settings().addChild(widget);
	}

}