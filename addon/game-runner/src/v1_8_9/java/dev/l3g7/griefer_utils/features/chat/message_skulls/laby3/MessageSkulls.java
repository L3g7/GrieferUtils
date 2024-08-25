/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.message_skulls.laby3;

import de.emotechat.addon.gui.chat.render.EmoteChatRenderer;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.labymod.laby3.util.ChatLineUtil;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.message_skulls.EmoteChatUtil;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;

@Singleton
@ExclusiveTo(LABY_3)
public class MessageSkulls extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<>(MESSAGE_PATTERNS) {{
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
			event.message = new ChatComponentText(id).appendSibling(event.message);
			return;
		}
	}


	@Mixin(value = EmoteChatRenderer.class, remap = false)
	@ExclusiveTo(LABY_3)
	private static class MixinEmoteChatRenderer {

		@Inject(method = "drawLine", at = @At("TAIL"))
		@SuppressWarnings("InvalidInjectorMethodSignature") // Library is obfuscated, so method parameters are as well
		public void injectDrawLine(FontRenderer font, ChatLine chatLine, float x, float y, int width, int alpha, int mouseX, int mouseY, CallbackInfo ci) {
			renderSkull(chatLine, y + 8, alpha / 255f);
		}

	}

	@Mixin(value = ChatRenderer.class, remap = false)
	@ExclusiveTo(LABY_3)
	private static class MixinChatRenderer {

		@Inject(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/DrawUtils;drawStringWithShadow(Ljava/lang/String;DDI)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
		private void injectRenderChat(int updateCounter, CallbackInfo ci, DrawUtils draw, int fontHeight, float scale, int chatLineCount, boolean chatOpen, float opacity, int width, int visibleMessages, double totalMessages, double animationSpeed, float lineHeight, double shift, double posX, double posY, int i, Iterator<ChatLine> chatLineIterator, ChatLine chatline, boolean firstLine, boolean lastLine, int updateCounterDifference, int alpha, int x, int y) {
			renderSkull(chatline, y, alpha / 255f);
		}

	}

	public static void renderSkull(ChatLine chatLine, double y, float alpha) {
		if (!FileProvider.getSingleton(MessageSkulls.class).isEnabled())
			return;

		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		IChatComponent component = (IChatComponent) chatLine.getComponent();
		String formattedText = component.getFormattedText();

		int idStart = formattedText.indexOf("§c   ");
		if (idStart == -1)
			return;

		IChatComponent wholeComponent = ChatLineUtil.getComponentFromLine(chatLine);
		// Part of an emote chat line
		if (wholeComponent == null)
			return;

		String msg = wholeComponent.getUnformattedText().replaceAll("§.", "");

		int startIndex = msg.indexOf('\u2503') + 2;
		int endIndex;
		int arrowIndex = msg.indexOf('\u00bb');

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

		if (Constants.EMOTECHAT && EmoteChatUtil.isEmote(chatLine))
			y += 4.5;

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		drawUtils.bindTexture(playerInfo.getLocationSkin());
		int x = drawUtils.getStringWidth(formattedText.substring(0, idStart)) + (formattedText.startsWith("§r§m§s") ? 2 : 1);
		drawUtils.drawTexture(x, y - 8, 32, 32, 32, 32, 8, 8, alpha); // First layer
		drawUtils.drawTexture(x, y - 8, 160, 32, 32, 32, 8, 8, alpha); // Second layer
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

}