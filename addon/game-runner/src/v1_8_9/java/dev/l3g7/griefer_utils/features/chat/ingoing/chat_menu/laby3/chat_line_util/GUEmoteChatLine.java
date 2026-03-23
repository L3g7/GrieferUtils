/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.laby3.chat_line_util;

import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.gui.ChatLineEntry;
import de.emotechat.addon.gui.chat.render.EmoteChatLine;
import de.emotechat.addon.gui.chat.render.EmoteChatRenderer;
import de.emotechat.addon.gui.chat.render.EmoteChatRendererType;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.minecraft.util.IChatComponent;

import java.util.Collection;

public class GUEmoteChatLine extends EmoteChatLine implements GUChatLine {

	private final IChatComponent modifiedComponent, originalComponent;

	public GUEmoteChatLine(IChatComponent modifiedComponent, IChatComponent originalComponent, Collection<ChatLineEntry> entries, boolean render, String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor) {
		super(entries, render, message, secondChat, room, component, updateCounter, chatLineId, highlightColor);
		this.modifiedComponent = modifiedComponent;
		this.originalComponent = originalComponent;
	}


	@Override
	public IChatComponent getModifiedComponent() {
		return modifiedComponent;
	}

	@Override
	public IChatComponent getOriginalComponent() {
		return originalComponent;
	}

	public static ChatLine tryCreatingEmoteChatLine(ChatRenderer emoteChatRenderer, IChatComponent modifiedComponent, IChatComponent originalComponent,  String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor) {
		if (!(emoteChatRenderer instanceof EmoteChatRendererType))
			return null;

		EmoteChatRenderer renderer = Reflection.get(emoteChatRenderer, "renderer");
		EmoteChatAddon addon = Reflection.get(renderer, "addon");
		Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(addon.getEmoteProvider().getIdSplitter(), message);
		if (entries.stream().noneMatch(ChatLineEntry::isEmote))
			return null;

		return new GUEmoteChatLine(modifiedComponent, originalComponent, entries, true, message, secondChat, room, component, updateCounter, chatLineId, highlightColor);
	}

}
