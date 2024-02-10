/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.laby4.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.context.ContextMenu;
import net.labymod.api.client.gui.screen.widget.context.ContextMenuEntry;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TagInputWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.HorizontalListEntry;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.ChatSettingActivity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

@Singleton
public class ChatFilterTemplates extends Feature { // NOTE: patch to FilterTemplates

	static final FilterTemplate[] TEMPLATES = new FilterTemplate[]{
		new FilterTemplate("Eingehende MSG", of("-> mir]"), of("»")),
		new FilterTemplate("Ausgehende MSG", of("[mir ->"), of("»")),
		new FilterTemplate("Globalchat", of("@["), of()),
		new FilterTemplate("Plotchat", of("[Plot-Chat]"), of("»")),
		new FilterTemplate("Eingehende Zahlung", of(" gegeben."), of("»", "->", ":", "Du hast")),
		new FilterTemplate("Ausgehende Zahlung", of(" gegeben."), of("»", "->", ":", "[GrieferGames]", "hat dir")),
		new FilterTemplate("MobRemover", of("[MobRemover]"), of("»", "->", ":")),
		new FilterTemplate("Clearlag", of("auf dem Boden liegende Items entfernt!", "[GrieferGames] Warnung! Die auf dem Boden liegenden Items werden in"), of("»", "->", ":")),
		new FilterTemplate("Greeting", of("[Greeting]"), of("»")),
		new FilterTemplate("Farewell", of("[Farewell]"), of("»")),
		new FilterTemplate("GrieferUtils", of("[GrieferUtils]"), of("»"))
	};

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Filtervorlagen")
		.description("Fügt Vorlagen bei LabyMods Chatfiltern hinzu.")
		.icon("labymod_3/filter");

	/**
	 * The setting that redirected to the currently open templateList.
	 */
	private static ListSetting filterList;

	/**
	 * The holder setting for the template selection.
	 */
	private static CategorySettingImpl templateList;

	public static void modifyAddButton(SettingContentActivity self) {
		if (!(self instanceof ChatSettingActivity))
			return;

		// Modify back button in template list
		if (self.getCurrentHolder() == templateList) {
			for (Widget child : self.document().getChild("content").getChildren().get(0).getChildren()) { // NOTE: replace with SettingActivityInitEvent#get / generalize
				if (child.hasId("back-button")) {
					ButtonWidget backButton = (ButtonWidget) ((HorizontalListEntry) child).childWidget();
					backButton.setPressable(() -> openSetting(self, filterList));
				}
			}
		}

		for (Widget child : self.document().getChild("content").getChildren().get(0).getChildren()) {
			if (child.hasId("add-button")) {
				filterList = (ListSetting) self.getCurrentHolder();
				ButtonWidget button = (ButtonWidget) ((HorizontalListEntry) child).childWidget();

				ContextMenu contextMenu = button.createContextMenu();

				// Add normal filter
				contextMenu.addEntry(ContextMenuEntry.builder()
					.icon(Textures.SpriteCommon.ADD)
					.text(Component.text("Neuer Filter"))
					.clickHandler(v -> {
						openSetting(self, filterList.createNew());
						return true;
					})
					.build());

				// Open template list
				contextMenu.addEntry(ContextMenuEntry.builder()
					.icon(Textures.SpriteCommon.ADD)
					.text(Component.text("Vorlage verwenden"))
					.clickHandler(v -> {
						// Open template list
						templateList = (CategorySettingImpl) CategorySetting.create()
							.name("Vorlagen");

						for (FilterTemplate template : TEMPLATES) {
							templateList.addSetting(
								ButtonSetting.create()
									.name(template.name)
									.buttonIcon(Textures.SpriteCommon.DARK_ADD)
									.callback(() -> loadTemplate(self, template)));
						}

						openSetting(self, templateList);
						return true;
					})
					.build());

				button.setPressable(button::openContextMenu);
			}
		}
	}

	private static void loadTemplate(SettingContentActivity activity, FilterTemplate ft) {
		Setting entry = filterList.createNew();

		TextFieldWidget name = (TextFieldWidget) ((SettingElement) entry.getById("name")).getWidgets()[0];
		name.setText(ft.name);

		TagInputWidget includeTags = (TagInputWidget) (((SettingElement) entry.getById("includeTags")).getWidgets()[0]);
		for (String contain : ft.contains)
			includeTags.tagCollection().add(contain);

		TagInputWidget excludeTags = (TagInputWidget) (((SettingElement) entry.getById("excludeTags")).getWidgets()[0]);
		for (String contain : ft.containsNot)
			excludeTags.tagCollection().add(contain);

		openSetting(activity, entry);
	}

	private static void openSetting(SettingContentActivity activity, Setting setting) {
		Function<Setting, Setting> screenCallback = Reflection.get(activity, "screenCallback");

		Reflection.set(activity, "currentHolder", setting);
		if (screenCallback != null)
			setting = screenCallback.apply(activity.getCurrentHolder());

		Reflection.set(activity, "currentHolder", setting);
		if (setting != null)
			activity.reload();
	}

	@Mixin(SettingContentActivity.class)
	public static class SettingContentActivityMixin {

		@Inject(method = "initialize", at = @At("RETURN"), remap = false)
		public void onToSettings(Parent parent, CallbackInfo ci) {
			modifyAddButton(c(this));
		}

	}

	private record FilterTemplate(String name, String[] contains, String[] containsNot) {}

	private static String[] of(String... args) {
		return args;
	}

}
