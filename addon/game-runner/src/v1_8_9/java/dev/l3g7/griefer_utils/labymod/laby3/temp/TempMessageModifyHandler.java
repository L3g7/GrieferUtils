/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.temp;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.labymod.laby3.bridges.LabyBridgeImpl;
import net.labymod.core.LabyModCore;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.ingamechat.renderer.MessageData;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.utils.manager.TagManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@ExclusiveTo(LABY_3)
public class TempMessageModifyHandler {

	private static final List<IChatComponent> packetMessages = Collections.synchronizedList(new LinkedList<>());
	private static IChatComponent lastMessage;

	@ExclusiveTo(LABY_3)
	@Mixin(value = ForgeEventFactory.class, remap = false)
	private static class MixinForgeEventFactory {

		@Inject(method = "onClientChat", at = @At("HEAD"))
		private static void injectOnClientChatHead(byte type, IChatComponent message, CallbackInfoReturnable<IChatComponent> cir) {
			packetMessages.add(message.createCopy());
		}

	    @Inject(method = "onClientChat", at = @At("RETURN"))
	    private static void injectOnClientChat(byte type, IChatComponent message, CallbackInfoReturnable<IChatComponent> cir) {
		    if (type == 2 || cir.getReturnValue() == null)
			    packetMessages.remove(packetMessages.size() - 1);
	    }

	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Shadow
		private IngameChatManager manager;

		private ChatDisplayAction cachedDisplayAction;
		private IChatComponent modifiedIChatComponent;
		private MessageData modifiedMessageData;

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;tagComponent(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
		private void injectSetChatLinePre(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			if (packetMessages.isEmpty())
				lastMessage = component.createCopy();
			else
				lastMessage = packetMessages.remove(0);
		}

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;"), locals = LocalCapture.CAPTURE_FAILHARD)
		private void injectSetChatLinePost(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci, ChatRenderer target, ChatDisplayAction displayAction, MessageData messageData) {
			cachedDisplayAction = displayAction;
		}

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;tagComponent(Ljava/lang/Object;)Ljava/lang/Object;"))
		private Object redirectTagging(Object component) {
			component = TagManager.tagComponent(component);
			IChatComponent icc = (IChatComponent) component;
			modifiedIChatComponent = (IChatComponent) ((LabyBridgeImpl) labyBridge).messageModifyConsumer.apply(lastMessage, component);
			return modifiedIChatComponent;
		}

		@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;"))
		private Filters.Filter redirectGetFilter(MessageData instance) {
			modifiedMessageData = manager.handleSwap(cachedDisplayAction, LabyModCore.getMinecraft().getChatComponent(modifiedIChatComponent));
			return modifiedMessageData.getFilter();
		}

		@ModifyVariable(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;", shift = At.Shift.AFTER), ordinal = 1, argsOnly = true)
		private boolean modifySecondChat(boolean oldValue) {
			return modifiedMessageData.isDisplayInSecondChat();
		}

	}

}
