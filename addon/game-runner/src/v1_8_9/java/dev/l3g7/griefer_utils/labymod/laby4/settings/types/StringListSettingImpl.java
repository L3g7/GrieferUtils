/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.temp.TempSettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import dev.l3g7.griefer_utils.core.settings.types.StringListSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.style.modifier.attribute.AttributeState;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.overlay.ScreenOverlay;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.bounds.ModifyReason;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static net.labymod.api.Textures.SpriteCommon.X;

public class StringListSettingImpl extends ListSetting implements StringListSetting, Laby4Setting<StringListSetting, List<String>> {

	private final ExtendedStorage<List<String>> storage;
	private String placeholder = "";
	private Icon entryIcon;

	public StringListSettingImpl() {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(new ArrayList<>());
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{Void.class};}

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		storage = new ExtendedStorage<>(list -> {
			JsonArray array = new JsonArray();
			list.forEach(s -> array.add(new JsonPrimitive(s)));
			return array;
		}, elem -> {
			List<String> list = new ArrayList<>();
			elem.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
			return list;
		}, new ArrayList<>());

		EventRegisterer.register(this);
	}

	@Override
	public ExtendedStorage<List<String>> getStorage() {
		return storage;
	}

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Component getDescription() {
		String description = storage.description;
		return description == null ? null : Component.text(description);
	}

	@Override
	public Icon getIcon() {
		return getStorage().icon;
	}

	@Override
	public StringListSetting placeholder(String placeholder) {
		this.placeholder = placeholder;
		return this;
	}

	@Override
	public StringListSetting entryIcon(Object icon) {
		this.entryIcon = Icons.of(icon);
		return this;
	}

	@EventListener
	private void onInit(TempSettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		List<String> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {

			ButtonSettingImpl entry = new ButtonSettingImpl();
			entry.name(values.get(i))
				.icon(entryIcon);

			int idx = i;
			event.settings().addChild(entry.createUnwrappedWidget(
				ButtonWidget.icon(
					Icons.of("pencil_vec"),
					() -> new StringListInputActivity(idx, event.activity).open()
				).addId("delete-button"), // Actually an edit button, but id is required for styling

				ButtonWidget.icon(X, () -> {
					values.remove(idx);
					notifyChange();
					event.activity.reload();
				}).addId("delete-button")
			));
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> new StringListInputActivity(-1, event.activity).open());

	}

	@AutoActivity
	@Link("string-list-input.lss")
	public class StringListInputActivity extends ScreenOverlay {

		private static final ModifyReason MODIFY_REASON_INITIAL_RESIZE = ModifyReason.of("initialResize");

		/**
		 * Which entry is being edited, -1 if adding a new entry.
		 */
		private final int editIndex;
		private final SettingContentActivity activity;

		private ButtonWidget addButton;

		public StringListInputActivity(int editIndex, SettingContentActivity activity) {
			super(32700);
			this.editIndex = editIndex;
			this.activity = activity;
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
			textInput.placeholder(Component.text(placeholder));
			textInput.setText(editIndex == -1 ? "" : get().get(editIndex));
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

			addButton = ButtonWidget.text(editIndex == -1 ? "Hinzufügen" : "Bearbeiten", () -> {
				if (editIndex == -1)
					get().add(textInput.getText().trim());
				else
					get().set(editIndex, textInput.getText().trim());

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
			activity.reload();
		}

	}

}
