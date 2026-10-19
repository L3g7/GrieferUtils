/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.ChatFilterWebhooks;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.filter.ChatFilter;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.configuration.labymod.chat.ChatTab;
import net.labymod.api.configuration.labymod.chat.ChatWindow;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig;
import net.labymod.api.event.client.chat.advanced.AdvancedChatTabMessageEvent;
import net.labymod.api.util.I18n;
import net.labymod.core.client.chat.advanced.DefaultChatWindow;
import net.labymod.core.client.chat.filter.DefaultFilterChatService;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatRuleDialogActivity;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatRuleDraft;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatTabDraft;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatTabDrafts;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.ChatFilterWebhooks.webhooks;

@ExclusiveTo(LABY_4)
public class FilterWebhooksLaby4 {

	public static final Map<ChatRuleDraft, String> webhookDrafts = new HashMap<>();

	@ExclusiveTo(LABY_4)
	@Mixin(value = ChatRuleDialogActivity.class, remap = false)
	public static abstract class ConfigMixin {

		@Shadow
		@Final
		private ChatRuleDraft rule;

		@Inject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDialogActivity;updateFooter()V"))
		public void injectWebhookWidget(VerticalListWidget<Widget> card, CallbackInfo ci) {
			String needle = I18n.translate("labymod.activity.chat.dialog.rule.then");

			for (Widget section : card.getChildren()) {
				for (Widget child : section.getChildren()) {
					if (child.hasId("chat-dialog-heading") && child instanceof ComponentWidget textWidget) {
						if (textWidget.component() instanceof TextComponent text) {
							if (text.getText().equals(needle)) {
								TextFieldWidget name = new TextFieldWidget();
								name.addId("dialog-field", "chat-dialog-field");
								name.maximalLength(999);
								name.placeholder(Component.text("https://discord.com/api/webhooks/..."));
								ChatRuleDraft draft = Reflection.get(this, "rule");
								name.setText(webhookDrafts.getOrDefault(draft, ""));
								name.updateListener((txt) -> {
									webhookDrafts.put(draft, txt);
									Reflection.invoke(this, "updateFooter");
								});
								FlexibleContentWidget row = Reflection.invoke(this, "row", Component.text("Discord-Webhook"), Component.text("Sendet die Nachricht in den Channel."), null);
								row.addFlexibleContent(name);

								//noinspection unchecked
								((AbstractWidget<Widget>) section).addChild(row);
								return;
							}
						}
					}
				}
			}
		}

		@ModifyArg(method = "updateFooter", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDialogActivity;showProblem(Lnet/labymod/api/client/component/Component;)V"))
		public Component injectValidation(Component problem) {
			if (problem != null)
				return problem;

			var webhook = webhookDrafts.getOrDefault(rule, "");
			if (webhook.isBlank())
				return null;

			if (!ChatFilterWebhooks.HOOK_URL_PATTERN.matcher(webhook).matches())
				return Component.text("kys");
			else
				return null;
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ChatTabDrafts.class, remap = false)
	public static class ChatTabDraftsMixin {

		@Inject(method = "write(Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDraft;)Lnet/labymod/api/client/chat/filter/ChatFilter;", at = @At("RETURN"))
		private static void injectWrite(ChatRuleDraft rule, CallbackInfoReturnable<ChatFilter> cir) {
			var filter = cir.getReturnValue().id().toString();
			var webhook = webhookDrafts.remove(rule);

			ChatFilterWebhooks.webhooks.put(filter, (webhook == null || webhook.isBlank()) ? null : webhook);
		}

		@Inject(method = "read(Lnet/labymod/api/client/chat/filter/ChatFilter;)Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDraft;", at = @At("RETURN"))
		private static void injectRead(ChatFilter filter, CallbackInfoReturnable<ChatRuleDraft> cir) {
			var webhook = ChatFilterWebhooks.webhooks.getOrDefault(filter.id().toString(), "");
			if (webhook == null)
				webhook = "";

			webhookDrafts.put(cir.getReturnValue(), webhook);
		}

		@Inject(method = "read(Lnet/labymod/api/configuration/labymod/chat/config/RootChatTabConfig;)Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatTabDraft;", at = @At("HEAD"))
		private static void injectClear(RootChatTabConfig root, CallbackInfoReturnable<ChatTabDraft> cir) {
			webhookDrafts.clear();
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ChatRuleDraft.class, remap = false)
	public static class ChatRuleDraftMixin {

		@Inject(method = "copy", at = @At("RETURN"))
		private void injectCopy(CallbackInfoReturnable<ChatRuleDraft> cir) {
			webhookDrafts.put(cir.getReturnValue(), webhookDrafts.getOrDefault((ChatRuleDraft) (Object) this, ""));
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultChatWindow.class, remap = false)
	public static class DefaultChatWindowMixin {

		@Inject(method = "save", at = @At("TAIL"))
		private void injectSave(CallbackInfo ci) {
			// Check for webhooks whose filters have been deleted
			List<String> orphans = new ArrayList<>(ChatFilterWebhooks.webhooks.keySet());
			for (ChatWindow window : Laby.references().advancedChatController().getWindows())
				for (ChatTab tab : window.getTabs())
					for (ChatFilter chatFilter : tab.config().filters().get())
						orphans.remove(chatFilter.id().toString());

			for (String orphan : orphans)
				ChatFilterWebhooks.webhooks.remove(orphan);

			ChatFilterWebhooks.saveWebhooks();
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultFilterChatService.class, remap = false)
	public static class DefaultFilterChatServiceMixin {

		@Final
		@Shadow
		private List<ChatFilter> matchingChatFilters;

		@Inject(method = "applyChatFilter", at = @At("RETURN"), remap = false)
		public void onApplyChatFilter(AdvancedChatTabMessageEvent event, CallbackInfo ci) {
			if (event.isCancelled())
				return;

			for (ChatFilter filter : matchingChatFilters) {
				Integer color = filter.shouldChangeBackground().get() ? filter.backgroundColor().get() : null;
				ChatFilterWebhooks.triggerWebhook(webhooks.get(filter.id().toString()), (IChatComponent) event.component(), filter.name().get(), color);
			}
		}

	}

}
