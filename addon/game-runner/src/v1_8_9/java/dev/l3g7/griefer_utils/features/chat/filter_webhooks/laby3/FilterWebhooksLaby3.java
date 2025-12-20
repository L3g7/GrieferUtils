/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.filter_webhooks.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.chat.filter_webhooks.FilterWebhooks;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class FilterWebhooksLaby3 extends FilterWebhooks {

	@OnEnable
	private void initialize() {
		init("laby3");
	}

	@EventListener
	public void onGuiOpen(GuiOpenEvent<GuiScreen> event) {
		if (event.gui instanceof GuiChatFilter)
			event.gui = new CustomGuiChatFilter(Reflection.get(event.gui, "defaultInputFieldText"));
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			if (refresh)
				return;

			// Check if filters match
			String msg = component.getUnformattedText().toLowerCase().replaceAll("§.", "");
			for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
				if (webhooks.containsKey(filter.getFilterName())
					&& !webhooks.get(filter.getFilterName()).trim().isEmpty()
					&& Arrays.stream(filter.getWordsContains()).anyMatch(w -> msg.contains(w.toLowerCase()))
					&& Arrays.stream(filter.getWordsContainsNot()).noneMatch(w -> msg.contains(w.toLowerCase()))) {

					String url = webhooks.get(filter.getFilterName());
					Integer color = filter.isHighlightMessage() ? ((filter.getHighlightColorR() & 0xff) << 16) | ((filter.getHighlightColorG() & 0xff) << 8) | (filter.getHighlightColorB() & 0xff) : null;
					triggerWebhook(url, component, filter.getFilterName(), color);
				}
			}
		}

	}

}