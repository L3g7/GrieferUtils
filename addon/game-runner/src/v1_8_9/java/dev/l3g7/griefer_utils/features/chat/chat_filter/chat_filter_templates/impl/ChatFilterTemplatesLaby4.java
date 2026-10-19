/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.ChatFilterTemplates;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.ChatFilterTemplates.FilterTemplate;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.core.client.chat.advanced.edit.ChatConditionDraft;
import net.labymod.core.client.chat.advanced.edit.ChatRuleDraft;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatDialogActivity;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatTabDialogActivity;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.part.ChatDialogWidgets;

import java.util.Arrays;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@ExclusiveTo(LABY_4)
public class ChatFilterTemplatesLaby4 {

	/*
	@ExclusiveTo(LABY_4)
	@Mixin(ChatTabDialogActivity.class)
	public static class ChatTabDialogActivityMixin {

		@Inject(method = "addRules", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/ChatTabDraft;picks()Z"), remap = false)
		public void onToSettings(VerticalListWidget<Widget> card, CallbackInfo ci) {
			Widget section = card.getChildren().getLast();
			HorizontalListWidget last = Reflection.c(section.getChildren().getLast());

			ButtonWidget button = ButtonWidget.component(Component.text("Vorlage verwenden"), IconsCommon.COPY,
				() -> Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new ChatRuleTemplateDialogActivity(c(this))));

			button.addId("dialog-action", "chat-dialog-add");
			last.addEntry(button);
		}

	}
	*/

	@AutoActivity
	public static class ChatRuleTemplateDialogActivity extends ChatDialogActivity {

		ChatTabDialogActivity tabDialog;

		public ChatRuleTemplateDialogActivity(ChatTabDialogActivity tabDialog) {
			super(tabDialog);
			this.tabDialog = tabDialog;
		}

		@Override
		protected String dialogId() {
			return "chat-rule-template-dialog";
		}

		@Override
		protected Component title() {
			return Component.text("Vorlagen");
		}

		@Override
		protected Component intro() {
			return Component.text("Wähle eine Vorlage aus, um sie einzufügen.");
		}

		@Override
		protected void fill(VerticalListWidget<Widget> card) {
			VerticalListWidget<Widget> rules = new VerticalListWidget<>();
			rules.addId("chat-dialog-rules");

			var chatRuleSummaryCls = Reflection.load("net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatRuleSummary");

			for (FilterTemplate template : ChatFilterTemplates.TEMPLATES) {
				ChatRuleDraft rule = buildRule(template);
				Component summary = Reflection.invoke(chatRuleSummaryCls, "of", rule);

				FlexibleContentWidget row = ChatDialogWidgets.row(Component.text(template.name), summary, null);
				ChatDialogWidgets.onCardPress(row, () -> Reflection.invoke(tabDialog, "editRule", rule, true));

				FlexibleContentWidget split = new FlexibleContentWidget();
				split.addId("chat-dialog-split");
				row.removeId("chat-dialog-row");
				row.addId("chat-dialog-split-main");
				split.addFlexibleContent(row);

				rules.addChild(split);
			}
			card.addChild(rules);
		}

		private ChatRuleDraft buildRule(FilterTemplate template) {
			var condition = ChatConditionDraft.empty();
			condition.include().addAll(Arrays.asList(template.contains));
			condition.exclude().addAll(Arrays.asList(template.containsNot));

			var rule = ChatRuleDraft.of(condition);
			if (template.highlighting) {
				rule.setHighlight(true);
				rule.setColor(
					// ARGB
					0xFF << 24
						| template.red << 16
						| template.green << 8
						| template.blue
				);
			}
			return rule;
		}

	}

}
