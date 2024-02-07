/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.settings.player_list;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.laby4.settings.types.ButtonSettingImpl;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
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
import net.labymod.api.client.gui.screen.widget.attributes.ObjectFitType;
import net.labymod.api.client.gui.screen.widget.overlay.ScreenOverlay;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.bounds.ModifyReason;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static net.labymod.api.Textures.SpriteCommon.X;

public class PlayerListSettingImpl extends ListSetting implements AbstractSetting<PlayerListSettingImpl, List<PlayerListEntry>>, BaseSettingImpl<PlayerListSettingImpl, List<PlayerListEntry>> { // NOTE: cleanup

	private final ExtendedStorage<List<PlayerListEntry>> storage;

	public PlayerListSettingImpl() {
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
			list.forEach(e -> array.add(new JsonPrimitive(e.id)));
			return array;
		}, elem -> {
			List<PlayerListEntry> list = new ArrayList<>();
			elem.getAsJsonArray().forEach(e -> list.add(new PlayerListEntry(null, e.getAsString())));
			return list;
		}, new ArrayList<>());

		EventRegisterer.register(this);
		init();
	}

	@Override
	public ExtendedStorage<List<PlayerListEntry>> getStorage() {
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

	public boolean contains(String name, UUID uuid) {
		if (name == null && uuid == null)
			return false;

		for (PlayerListEntry entry : get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.id) : name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		List<PlayerListEntry> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			PlayerListEntry value = values.get(i);

			ButtonSettingImpl entry = new ButtonSettingImpl();
			entry.name(value.name)
				.icon(Icon.head(value.name));

			entry.setParent((Setting) this);

			int idx = i;
			event.settings().addChild(entry.createUnwrappedWidget(
				ButtonWidget.icon(
					SettingsImpl.buildIcon("pencil_vec"),
					() -> new PlayerListInputActivity(idx, event.activity).open()
				).addId("delete-button"), // Actually an edit button, but id is required for styling

				ButtonWidget.icon(X, () -> {
					values.remove(idx);
					notifyChange();
					event.activity.reload();
				}).addId("delete-button")
			));
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> new PlayerListInputActivity(-1, event.activity).open());

	}

	@AutoActivity
	@Link("player-list-input.lss")
	public class PlayerListInputActivity extends ScreenOverlay {

		private static final ModifyReason MODIFY_REASON_INITIAL_RESIZE = ModifyReason.of("initialResize");

		/**
		 * Which entry is being edited, -1 if adding a new entry.
		 */
		private final int editIndex;
		private final SettingContentActivity activity;
		private TextFieldWidget textInput;
		private IconWidget previewWidget;
		private ButtonWidget addButton;

		public PlayerListInputActivity(int editIndex, SettingContentActivity activity) {
			super(32700);
			this.editIndex = editIndex;
			this.activity = activity;
		}

		public void initialize(Parent parent) { // NOTE: duplicate code
			super.initialize(parent);

			document.addChild(new DivWidget().addId("background"));

			DivWidget root = new DivWidget().addId("root");
			document.addChild(root);

			VerticalListWidget<Widget> rows = new VerticalListWidget<>();
			root.addChild(rows);

			String defaultName = editIndex == -1 ? "" : get().get(editIndex).name;

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

			addButton = ButtonWidget.text(editIndex == -1 ? "Hinzufügen" : "Bearbeiten", () -> {
				String name = textInput.getText().trim();
				if (editIndex == -1)
					get().add(PlayerListEntry.getEntry(name));
				else
					get().set(editIndex, PlayerListEntry.getEntry(name));

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
			return name.isBlank() ? Icon.head(UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838")) : Icon.head(name.trim());
		}

		private void update() {
			String name = textInput.getText().trim();
			PlayerListEntry entry = PlayerListEntry.getEntry(name); // NOTE: use LabyMod's player resolved?
			if (!entry.exists) {
				textInput.textColor().set(0xFFFF0000);
				addButton.setEnabled(false);
			} else {
				textInput.textColor().set(0xFFFFFFFF);
				addButton.setEnabled(entry.loaded);
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
