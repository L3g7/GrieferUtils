/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types.list;

import dev.l3g7.griefer_utils.core.settings.types.list.StringListEntry;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.style.modifier.attribute.AttributeState;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.overlay.ScreenOverlay;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.util.bounds.ModifyReason;

public class StringListSettingImpl extends ListSettingImpl<StringListEntry> {

	public StringListSettingImpl() {
		super(StringListEntry.class);
		customEdit(e -> new StringListInputActivity(e).open());
	}

	@AutoActivity
	@Link("string-list-input.lss")
	public class StringListInputActivity extends ScreenOverlay {

		private static final ModifyReason MODIFY_REASON_INITIAL_RESIZE = ModifyReason.of("initialResize");

		private final StringListEntry entry;

		private ButtonWidget addButton;

		public StringListInputActivity(StringListEntry entry) {
			super(32700);
			this.entry = entry;
		}

		public void initialize(Parent parent) {
			super.initialize(parent);

			document.addChild(new DivWidget().addId("background"));

			DivWidget root = new DivWidget().addId("root");
			document.addChild(root);

			VerticalListWidget<Widget> rows = new VerticalListWidget<>();
			root.addChild(rows);

			// Text input
			TextFieldWidget textInput = new TextFieldWidget();
			rows.addChild(textInput);
			textInput.setText(entry == null ? "" : entry.get());
			textInput.setCursorAtEnd();
			textInput.maximalLength(100);
			textInput.setFocused(true);
			textInput.submitHandler(name -> {
				if (addButton.isAttributeStateEnabled(AttributeState.ENABLED))
					addButton.onPress();
			});

			// Buttons
			HorizontalListWidget buttons = new HorizontalListWidget();
			rows.addChild(buttons);

			buttons.addEntry(ButtonWidget.text("Abbrechen", this::close));

			addButton = ButtonWidget.text(entry == null ? "Hinzufügen" : "Bearbeiten", () -> {
				if (entry == null)
					StringListSettingImpl.this.add(new StringListEntry(textInput.getText().trim()));
				else
					entry.set(textInput.getText().trim());

				notifyChange();
				close();
			});
			buttons.addEntry(addButton);
			textInput.updateListener(s -> addButton.setEnabled(!s.trim().isEmpty()));
			addButton.setEnabled(!textInput.getText().isEmpty());
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

			Widget root = document.getChild("root");
			root.bounds().setX(bounds().getCenterX() + 62 - root.bounds().getWidth() / 2, MODIFY_REASON_INITIAL_RESIZE);
			root.bounds().setY(bounds().getCenterY() + 8 - root.bounds().getHeight() / 2, MODIFY_REASON_INITIAL_RESIZE);
		}

		public void open() {
			Laby.labyAPI().screenOverlayHandler().registerOverlay(this);
			setActive(true);
		}

		public void close() {
			setActive(false);
			Laby.labyAPI().screenOverlayHandler().unregisterOverlay(this);
			if (Laby4Util.getActivity() instanceof Activity activity)
				activity.reload();
		}
	}
}
