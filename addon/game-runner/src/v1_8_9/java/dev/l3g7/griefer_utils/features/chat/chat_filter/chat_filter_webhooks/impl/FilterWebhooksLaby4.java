/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.ChatFilterWebhooks;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatMessage;
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
import net.labymod.core.client.chat.advanced.edit.ChatConditionDraft;
import net.labymod.core.client.chat.advanced.edit.ChatRuleDraft;
import net.labymod.core.client.chat.advanced.edit.ChatTabDraft;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.part.ChatDialogWidgets;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@ExclusiveTo(LABY_4)
public class FilterWebhooksLaby4 {

	public static final Map<ChatConditionDraft, String> webhookDrafts = new HashMap<>();

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatRuleDialogActivity", remap = false)
	public static abstract class ConfigMixin {

		@Shadow
		@Final
		private ChatRuleDraft rule;

		@Shadow
		protected abstract void updateThen();

		@Inject(method = "fillLast", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDialogActivity;updateThen()V"), require = 0)
		public void injectWebhookWidget(VerticalListWidget<Widget> card, CallbackInfo ci) {
			if (!ChatFilterWebhooks.enabled.get())
				return;

			String needle = I18n.translate("labymod.activity.chat.dialog.rule.then.title");

			for (Widget section : card.getChildren()) {
				for (Widget child : section.getChildren()) {
					if (child.hasId("chat-dialog-heading") && child instanceof ComponentWidget textWidget) {
						if (textWidget.component() instanceof TextComponent text) {
							if (text.getText().equals(needle)) {
								TextFieldWidget name = new TextFieldWidget();
								name.addId("dialog-field", "chat-dialog-field");
								name.maximalLength(999);
								name.placeholder(Component.text("https://discord.com/api/webhooks/..."));
								ChatConditionDraft draft = this.rule.condition();
								name.setText(webhookDrafts.getOrDefault(draft, ""));
								name.updateListener((txt) -> {
									webhookDrafts.put(draft, txt);
									this.updateThen();
								});
								FlexibleContentWidget row = ChatDialogWidgets.row(Component.text("Discord-Webhook"), Component.text("Sendet die Nachricht in den Channel."), null);
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

		@ModifyArg(method = "updateThen", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatRuleDialogActivity;showProblem(Lnet/labymod/api/client/component/Component;)V"), require = 0)
		public Component injectValidation(Component problem) {
			if (problem != null)
				return problem;

			var webhook = webhookDrafts.getOrDefault(rule.condition(), "");
			if (webhook.isBlank())
				return null;

			if (!ChatFilterWebhooks.HOOK_URL_PATTERN.matcher(webhook).matches())
				return Component.text("Ungültiger Webhook");
			else
				return null;
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.chat.advanced.edit.ChatTabDrafts", remap = false)
	public static class ChatTabDraftsMixin {

		@Inject(method = "write(Lnet/labymod/core/client/chat/advanced/edit/ChatConditionDraft;Lnet/labymod/api/client/chat/filter/ChatFilter;)V", at = @At("RETURN"), require = 0)
		private static void injectWrite(ChatConditionDraft condition, ChatFilter filter, CallbackInfo ci) {
			var filterId = filter.id().toString();
			var webhook = webhookDrafts.remove(condition);

			ChatFilterWebhooks.webhooks.put(filterId, (webhook == null || webhook.isBlank()) ? null : webhook);
			ChatFilterWebhooks.saveWebhooks();
		}

		@Inject(method = "read(Lnet/labymod/api/client/chat/filter/ChatFilter;)Lnet/labymod/core/client/chat/advanced/edit/ChatConditionDraft;", at = @At("RETURN"), require = 0)
		private static void injectRead(ChatFilter filter, CallbackInfoReturnable<ChatConditionDraft> cir) {
			var webhook = ChatFilterWebhooks.webhooks.getOrDefault(filter.id().toString(), "");
			if (webhook == null)
				webhook = "";

			webhookDrafts.put(cir.getReturnValue(), webhook);
		}

		@Inject(method = "read(Lnet/labymod/api/configuration/labymod/chat/config/RootChatTabConfig;Ljava/util/Collection;)Lnet/labymod/core/client/chat/advanced/edit/ChatTabDraft;", at = @At("HEAD"), require = 0)
		private static void injectClear(RootChatTabConfig root, Collection<ChatWindow> windows, CallbackInfoReturnable<ChatTabDraft> cir) {
			webhookDrafts.clear();
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.chat.advanced.edit.ChatConditionDraft", remap = false)
	public static class ChatRuleDraftMixin {

		@Inject(method = "copy", at = @At("RETURN"), require = 0)
		private void injectCopy(CallbackInfoReturnable<ChatConditionDraft> cir) {
			webhookDrafts.put(cir.getReturnValue(), webhookDrafts.getOrDefault((ChatConditionDraft) (Object) this, ""));
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.chat.advanced.DefaultChatWindow", remap = false)
	public static class DefaultChatWindowMixin {

		@Inject(method = "save", at = @At("TAIL"), require = 0)
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

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.chat.filter.DefaultFilterChatService", remap = false)
	public static abstract class DefaultFilterChatServiceMixin {

		@Final
		@Shadow
		private List<ChatFilter> matchingChatFilters;

		@Shadow
		protected abstract void refreshMatchingFilters(List<? extends ChatFilter> filters, ChatMessage chatMessage);

		@Inject(method = "applyChatFilter", at = @At("RETURN"), require = 0)
		public void onApplyChatFilter(AdvancedChatTabMessageEvent event, CallbackInfo ci) {
			if (event.isCancelled())
				return;

			refreshMatchingFilters(event.tab().config().filters().get(), event.message().chatMessage());
			refreshMatchingFilters(event.tab().config().rules().get(), event.message().chatMessage());

			for (ChatFilter filter : matchingChatFilters) {
				Integer color = filter.shouldChangeBackground().get() ? filter.backgroundColor().get() : null;
				String url = ChatFilterWebhooks.webhooks.get(filter.id().toString());
				ChatFilterWebhooks.triggerWebhook(url, (IChatComponent) event.component(), filter.name().get(), color);
			}
		}

	}

}
