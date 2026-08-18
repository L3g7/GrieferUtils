/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.ChatMenu.ChatMenuBridge;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.ChatInputOverlay;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ChatMenuLaby4 implements ChatMenuBridge {

	@Override
	public boolean isChatOpen() {
		return Laby4Util.getActivity() instanceof ChatInputOverlay;
	}

	@Override
	public Pair<IChatComponent, IChatComponent> getHoveredComponent() {
		return ChatLineUtil.getHoveredComponent();
	}

}