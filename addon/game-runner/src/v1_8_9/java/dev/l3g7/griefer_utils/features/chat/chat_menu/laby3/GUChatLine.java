/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby3;

import net.labymod.ingamechat.renderer.ChatLine;
import net.minecraft.util.IChatComponent;

public class GUChatLine extends ChatLine {

	public final IChatComponent modifiedComponent, originalComponent;

	public GUChatLine(IChatComponent modifiedComponent, IChatComponent originalComponent, String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor) {
		super(message, secondChat, room, component, updateCounter, chatLineId, highlightColor);
		this.modifiedComponent = modifiedComponent;
		this.originalComponent = originalComponent;
	}

}
