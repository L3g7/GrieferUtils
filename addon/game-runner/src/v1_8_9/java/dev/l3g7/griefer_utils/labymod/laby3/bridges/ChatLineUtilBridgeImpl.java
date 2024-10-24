/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.ChatLineUtilBridge;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.utils.manager.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.labymod.ingamechat.IngameChatManager.INSTANCE;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class ChatLineUtilBridgeImpl implements ChatLineUtilBridge {

	public static IChatComponent currentComponent;

	public static final Map<ChatLine, IChatComponent> LINE_TO_COMPONENT = new HashMap<>();
	public static final List<IChatComponent> MODIFIED_COMPONENTS = new ArrayList<>();
	public static final List<IChatComponent> UNMODIFIED_COMPONENTS = new ArrayList<>();

	@Override
	public IChatComponent getHoveredComponent() {
		for (ChatRenderer chatRenderer : INSTANCE.getChatRenderers()) {
			IChatComponent component = getHoveringComponent(chatRenderer);
			if (component != null)
				return component;
		}

		return null;
	}

	@Override
	public IChatComponent getUnmodified(IChatComponent iChatComponent) {
		if (iChatComponent == null)
			return null;

		int index = MODIFIED_COMPONENTS.indexOf(iChatComponent);

		if (index == -1)
			return null;

		return UNMODIFIED_COMPONENTS.get(index);
	}

	private static IChatComponent getHoveringComponent(ChatRenderer chatRenderer) {
		if (!chatRenderer.isChatOpen() || !chatRenderer.isMouseOver())
			return null;

		float mouseX = chatRenderer.isRightBound() ? chatRenderer.lastMouseX - chatRenderer.getChatPositionX() + chatRenderer.getChatWidth() + 3 : -(chatRenderer.getChatPositionX() - chatRenderer.lastMouseX);
		float mouseY = -chatRenderer.lastMouseY + chatRenderer.getChatPositionY();
		mouseX /= chatRenderer.getChatScale();
		mouseY /= chatRenderer.getChatScale();
		List<ChatLine> list = new LinkedList<>();

		for (ChatLine chatline : chatRenderer.getChatLines()) {
			if (chatline != null && chatline.getRoom().equals(INSTANCE.getSelectedRoom())) {
				list.add(chatline);
			}
		}

		int hoveredLine = (int) mouseY / Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + chatRenderer.getScrollPos();
		if (hoveredLine < 0 || hoveredLine >= list.size())
			return null;

		ChatLine chatline = list.get(hoveredLine);
		IChatComponent lineComponent = (IChatComponent) chatline.getComponent();
		int x = mc().fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(lineComponent.getFormattedText(), false));
		if (x <= mouseX)
			return null;

		return LINE_TO_COMPONENT.get(chatline);
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = ChatRenderer.class, remap = false)
	private static class MixinChatRenderer {

		@Redirect(method = "addChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
		public void postChatLineAddEvent(List<Object> instance, int i, Object o) {
			LINE_TO_COMPONENT.put((ChatLine) o, currentComponent);
			instance.add(i, o);
		}

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			currentComponent = component;
		}

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;"))
		public void postMessageModifiedEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			MODIFIED_COMPONENTS.add(component);
		}

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = TagManager.class, remap = false)
	private static class MixinTagManager {

		private static IChatComponent originalMessage;

		@ModifyVariable(method = "tagComponent", at = @At("HEAD"), ordinal = 0, argsOnly = true)
		private static Object injectTagComponent(Object value) {
			UNMODIFIED_COMPONENTS.add(((IChatComponent) value).createCopy());
			return value;
		}

	}

}
