/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types.list;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerListEntry;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.style.modifier.attribute.AttributeState;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.ObjectFitType;
import net.labymod.api.client.gui.screen.widget.overlay.ScreenOverlay;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.bounds.ModifyReason;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsActivity;

import java.util.UUID;

public class PlayerListSettingImpl extends ListSettingImpl<PlayerListEntry> {

	public PlayerListSettingImpl() {
		super(PlayerListEntry.class);
		customEdit(e -> new PlayerListInputActivity(e).open());
	}

	@Override
	protected Icon buildIcon(PlayerListEntry entry) {
		if (entry.getSkin() == null)
			return super.buildIcon(entry);

		if (entry.isJava()) {
			if (entry.isValid() || entry.getId() == null)
				return Icon.head((ResourceLocation) entry.getSkin());
		} else
			return Icon.texture((ResourceLocation) entry.getSkin());

		return super.buildIcon(entry);
	}

	@AutoActivity
	@Link("player-list-input.lss")
	public class PlayerListInputActivity extends ScreenOverlay {

		private static final ModifyReason MODIFY_REASON_INITIAL_RESIZE = ModifyReason.of("initialResize");

		private final PlayerListEntry entry;

		private TextFieldWidget textInput;
		private IconWidget previewWidget;
		private ButtonWidget addButton;

		public PlayerListInputActivity(PlayerListEntry entry) {
			super(32700);
			this.entry = entry;
		}

		public void initialize(Parent parent) { // NOTE: duplicate code
			super.initialize(parent);

			document.addChild(new DivWidget().addId("background"));

			DivWidget root = new DivWidget().addId("root");
			document.addChild(root);

			VerticalListWidget<Widget> rows = new VerticalListWidget<>();
			root.addChild(rows);

			String defaultName = entry == null ? "" : entry.getName();

			// Player preview
			previewWidget = new IconWidget(createIcon(defaultName));
			previewWidget.objectFit().set(ObjectFitType.CONTAIN);
			rows.addChild(previewWidget);

			// Text input
			textInput = new TextFieldWidget();
			textInput.placeholder(Component.text("Spielername"));
			textInput.setText(defaultName);
			textInput.setCursorAtEnd();
			textInput.maximalLength(100);
			textInput.setFocused(true);
			textInput.updateListener(name -> {
				previewWidget.icon().set(createIcon(name));
				update();
			});
			textInput.submitHandler(name -> {
				if (addButton.isAttributeStateEnabled(AttributeState.ENABLED))
					addButton.onPress();
			});
			rows.addChild(textInput);

			// Buttons
			HorizontalListWidget buttons = new HorizontalListWidget();
			rows.addChild(buttons);

			buttons.addEntry(ButtonWidget.text("Abbrechen", this::close));

			addButton = ButtonWidget.text(entry == null ? "Hinzufügen" : "Bearbeiten", () -> {
				String name = textInput.getText().trim();
				if (entry == null)
					PlayerListSettingImpl.this.add(PlayerListEntry.fromName(name));
				else
					entry.copyFrom(PlayerListEntry.fromName(name));

				notifyChange();
				close();
			});
			buttons.addEntry(addButton);
			addButton.setEnabled(!defaultName.isEmpty());
		}

		@Override
		public void tick() {
			super.tick();
			update();
		}

		private Icon createIcon(String name) {
			return name.isBlank() ? Icon.head(UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838")) /* MHF_Question */ : Icon.head(name.trim());
		}

		private void update() {
			String name = textInput.getText().trim();
			PlayerListEntry entry = PlayerListEntry.fromName(name);
			if (!entry.isValid()) {
				textInput.textColor().set(0xFFFF0000);
				addButton.setEnabled(false);
			} else {
				textInput.textColor().set(0xFFFFFFFF);
				addButton.setEnabled(entry.isLoaded());
			}
		}

		@Override
		public boolean keyPressed(Key key, InputType type) {
			boolean result = super.keyPressed(key, type);
			if (!result && key == Key.ESCAPE) {
				close();
				return true;
			}

			return result;
		}

		@Override
		protected void postStyleSheetLoad() {
			super.postStyleSheetLoad();

			// Center root widget
			Widget root = document.getChild("root");
			root.bounds().setX(bounds().getCenterX() - root.bounds().getWidth() / 2, MODIFY_REASON_INITIAL_RESIZE);
			root.bounds().setY(bounds().getCenterY() - root.bounds().getHeight() / 2, MODIFY_REASON_INITIAL_RESIZE);
		}

		public void open() {
			Laby.labyAPI().screenOverlayHandler().registerOverlay(this);
			setActive(true);
		}

		public void close() {
			setActive(false);
			Laby.labyAPI().screenOverlayHandler().unregisterOverlay(this);
			if (Laby4Util.getModsActivity() instanceof ModsActivity modsActivity)
				Reflection.invoke(modsActivity, "reloadWithoutTransitions");
			else if (Laby4Util.getActivity() instanceof Activity activity)
				activity.reload();
		}

	}

}
