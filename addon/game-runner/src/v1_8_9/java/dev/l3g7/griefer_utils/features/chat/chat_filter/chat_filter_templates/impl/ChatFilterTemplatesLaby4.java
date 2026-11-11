/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.ChatFilterTemplates;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_templates.ChatFilterTemplates.FilterTemplate;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.core.client.chat.advanced.edit.ChatConditionDraft;
import net.labymod.core.client.chat.advanced.edit.ChatDialogSteps;
import net.labymod.core.client.chat.advanced.edit.ChatRuleDraft;
import net.labymod.core.client.chat.advanced.edit.ChatTabPreset;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.ChatRuleDialogActivity;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.part.ChatDialogWidgets;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.part.ChatStartPicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@ExclusiveTo(LABY_4)
public class ChatFilterTemplatesLaby4 {

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.gui.screen.activity.activities.ingame.chat.dialog.part.ChatStartPicker", remap = false)
	public static class ChatStartPickerMixin {

		@ModifyArg(method = "presetCards", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/ingame/chat/dialog/part/ChatDialogWidgets;choices([Lnet/labymod/api/client/gui/screen/widget/Widget;)Lnet/labymod/api/client/gui/screen/widget/Widget;"), require = 0)
		private Widget[] modifyCards(Widget[] cards) {
			return ChatFilterTemplatesLaby4.modifyCards(cards, (ChatStartPicker) (Object) this);
		}

		@Inject(method = "tiles", at = @At(value = "RETURN"), require = 0)
		private void modifyTiles(CallbackInfoReturnable<Widget> cir) {
			ChatFilterTemplatesLaby4.modifyTiles(cir.getReturnValue(), (ChatStartPicker) (Object) this);
		}

	}

	private static void addHeader(Consumer<Widget> addFn) {
		ComponentWidget note = ComponentWidget.component(Component.text(""));
		note.addId("chat-dialog-note");
		addFn.accept(note);

		ComponentWidget heading = ComponentWidget.component(Component.text("GrieferUtils Vorlagen"));
		heading.addId("chat-dialog-heading");
		addFn.accept(heading);
	}

	public static Widget[] modifyCards(Widget[] cards, ChatStartPicker picker) {
		if (!ChatFilterTemplates.get().isEnabled())
			return cards;

		List<Widget> list = new ArrayList<>(Arrays.asList(cards));
		addHeader(list::add);

		for (FilterTemplate template : ChatFilterTemplates.TEMPLATES) {
			FlexibleContentWidget card = ChatDialogWidgets.card(Icons.of(template.icon),
				Component.text(template.name), null, null);

			ChatDialogWidgets.onCardPress(card, () -> onPress(template, picker));
			list.add(card);

		}

		return list.toArray(new Widget[0]);
	}

	public static void modifyTiles(Widget tilesRT, ChatStartPicker picker) {
		if (!ChatFilterTemplates.get().isEnabled())
			return;

		@SuppressWarnings("unchecked")
		VerticalListWidget<Widget> tiles = (VerticalListWidget<Widget>) tilesRT;
		addHeader(tiles::addChild);

		FlexibleContentWidget row = null;
		for (FilterTemplate template : ChatFilterTemplates.TEMPLATES) {
			if (row == null) {
				row = new FlexibleContentWidget();
				row.addId("chat-dialog-tiles-row");
				tiles.addChild(row);
			}

			ButtonWidget tile = ButtonWidget.component(Component.text(template.name), Icons.of(template.icon), () -> onPress(template, picker));
			tile.addId("chat-dialog-tile");

			row.addFlexibleContent(tile);
			if (row.getChildren().size() == 2) {
				row = null;
			}
		}
	}

	private static void onPress(FilterTemplate template, ChatStartPicker picker) {
		Reflection.set(picker, "words", List.of());
		var selected = Reflection.get(picker, "selected");
		if (selected != null)
			Reflection.set(picker, "selected", ChatTabPreset.SOMETHING_ELSE);

		ChatDialogWidgets.playButtonPress();

		var activity = Laby4Util.getActivity();
		if (!(activity instanceof ChatRuleDialogActivity))
			throw new IllegalStateException();

		// Load template
		ChatDialogSteps steps = Reflection.invoke(activity, "steps");
		var draft = ChatConditionDraft.empty();
		draft.include().addAll(Arrays.asList(template.contains));
		draft.exclude().addAll(Arrays.asList(template.containsNot));

		steps.choose(ChatTabPreset.SOMETHING_ELSE, draft);
		ChatRuleDraft rule = Reflection.get(activity, "rule");

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

		activity.reload();
	}

}
