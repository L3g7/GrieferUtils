/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;


import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.api.misc.Constants.*;

@Singleton
public class MessageSkulls extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<Pattern>(MESSAGE_PATTERNS) {{
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten an.")
		.icon("steve");

	@EventListener
	public void onMsgReceive(MessageEvent.MessageModifyEvent event) {
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.original.getFormattedText());
			if (!matcher.matches())
				continue;

			String id = "§c   §r";
			event.setMessage(new ChatComponentText(id).appendSibling(event.message));
			return;
		}
	}

	/*
	// FIXME: RenderChatEvent
	@EventListener
	public void renderSkull(RenderChatEvent event) {
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		IChatComponent component = (IChatComponent) event.chatLine.getComponent();
		String formattedText = component.getFormattedText();

		int idStart = formattedText.indexOf("§c   ");
		if (idStart == -1)
			return;

		IChatComponent wholeComponent = ChatLineUtil.getComponentFromLine(event.chatLine);
		// Part of an emote chat line
		if (wholeComponent == null)
			return;

		String msg = wholeComponent.getUnformattedText();

		int startIndex = msg.indexOf('\u2503') + 2;
		int endIndex;
		int arrowIndex = msg.indexOf('\u00bb');

		if (idStart > startIndex)
			return;

		IChatComponent unmodified = ChatLineUtil.getUnmodifiedIChatComponent(wholeComponent);
		if (unmodified == null)
			throw new RuntimeException("ChatLine could not be unmodified! " + wholeComponent);

		String uMsg = unmodified.getUnformattedText();

		if (arrowIndex != -1)
			endIndex = arrowIndex - 1;
		else if (uMsg.startsWith("[Plot-Chat]"))
			endIndex = msg.indexOf(':', startIndex) - 1;
		else if (uMsg.startsWith("[") && uMsg.contains(" -> mir]"))
			endIndex = msg.indexOf('-', startIndex) - 1;
		else if (uMsg.startsWith("[mir -> "))
			endIndex = msg.indexOf(']', startIndex);
		else
			endIndex = msg.indexOf(' ', startIndex);

		String name;
		try {
			name = msg.substring(startIndex, endIndex);
		} catch (StringIndexOutOfBoundsException e) {
			BugReporter.reportError(new Throwable(String.format("StringIndexOutOfBoundsException %s / %s with indices %d / %d", uMsg, msg, startIndex, endIndex)));
			return;
		}
		NetworkPlayerInfo playerInfo = MinecraftUtil.mc().getNetHandler().getPlayerInfo(NameCache.ensureRealName(name));
		if (playerInfo == null)
			return;

		int y = event.y;

		if (Constants.EMOTECHAT && EmoteChatUtil.isEmote(event.chatLine))
			y += 4.5;

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		drawUtils.bindTexture(playerInfo.getLocationSkin());
		int x = drawUtils.getStringWidth(formattedText.substring(0, idStart)) + (formattedText.startsWith("§r§m§s") ? 2 : 1);
		drawUtils.drawTexture(x, y - 8, 32, 32, 32, 32, 8, 8, event.alpha); // First layer
		drawUtils.drawTexture(x, y - 8, 160, 32, 32, 32, 8, 8, event.alpha); // Second layer
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}*/

}