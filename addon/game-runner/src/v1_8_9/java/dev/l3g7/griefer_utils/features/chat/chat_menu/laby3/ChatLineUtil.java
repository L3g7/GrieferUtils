/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.utils.manager.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@ExclusiveTo(LABY_3)
public class ChatLineUtil {

	public static Pair<IChatComponent, IChatComponent> getHoveredComponent() {
		for (ChatRenderer chatRenderer : IngameChatManager.INSTANCE.getChatRenderers()) {
			if (!chatRenderer.isChatOpen() || !chatRenderer.isMouseOver())
				continue;

			// Get mouse position
			float mouseX = chatRenderer.isRightBound() ? chatRenderer.lastMouseX - chatRenderer.getChatPositionX() + chatRenderer.getChatWidth() + 3 : -(chatRenderer.getChatPositionX() - chatRenderer.lastMouseX);
			float mouseY = -chatRenderer.lastMouseY + chatRenderer.getChatPositionY();
			mouseX /= chatRenderer.getChatScale();
			mouseY /= chatRenderer.getChatScale();
			List<ChatLine> list = new LinkedList<>();

			// Get chat line
			for (ChatLine chatline : chatRenderer.getChatLines())
				if (chatline != null && chatline.getRoom().equals(IngameChatManager.INSTANCE.getSelectedRoom()))
					list.add(chatline);

			int hoveredLine = (int) mouseY / Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + chatRenderer.getScrollPos();
			if (hoveredLine < 0 || hoveredLine >= list.size())
				continue;

			// Check mouse X
			GUChatLine chatline = (GUChatLine) list.get(hoveredLine);
			IChatComponent lineComponent = (IChatComponent) chatline.getComponent();
			int x = mc().fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(lineComponent.getFormattedText(), false));
			if (x <= mouseX)
				continue;

			return new Pair<>(chatline.modifiedComponent, chatline.originalComponent);
		}

		return null;
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Unique
		private IChatComponent grieferUtils$unmodifiedComponent;
		@Unique
		private IChatComponent grieferUtils$modifiedComponent;

		@Inject(method = "setChatLine", at = @At("HEAD"))
		public void injectSetChatLineHead(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			grieferUtils$unmodifiedComponent = component;
			grieferUtils$modifiedComponent = component;
		}

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;tagComponent(Ljava/lang/Object;)Ljava/lang/Object;"))
		public Object injectSetChatLineTagComponent(Object a) {
			IChatComponent modifiedComponent = (IChatComponent) TagManager.tagComponent(a);
			grieferUtils$modifiedComponent = modifiedComponent;
			return modifiedComponent;
		}

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;addChatLine(Ljava/lang/String;ZLjava/lang/String;Ljava/lang/Object;IILjava/lang/Integer;Z)V"))
		public void redirectAddLine(ChatRenderer instance, String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor, boolean refresh) {
			instance.getChatLines().add(0, new GUChatLine(grieferUtils$modifiedComponent, grieferUtils$unmodifiedComponent, message, secondChat, room, component, updateCounter, chatLineId, highlightColor));
			if (!refresh)
				Reflection.set(instance, "animationShift", System.currentTimeMillis());
		}

	}

}
