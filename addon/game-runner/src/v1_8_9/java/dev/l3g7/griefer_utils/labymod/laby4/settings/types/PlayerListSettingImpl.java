/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.settings.types.player_list.PlayerListEntry;
import dev.l3g7.griefer_utils.core.settings.types.player_list.PlayerListEntryResolver;
import dev.l3g7.griefer_utils.core.settings.types.player_list.PlayerListSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
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
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.SettingPermissionHolder;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.bounds.ModifyReason;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class PlayerListSettingImpl extends ListSetting implements Laby4Setting<PlayerListSetting, List<PlayerListEntry>>, PlayerListSetting { // NOTE: cleanup

	private final ExtendedStorage<List<PlayerListEntry>> storage;

	public PlayerListSettingImpl() {
		super(UUID.randomUUID().toString(), null, null, new String[0], (SettingPermissionHolder) null, null, (byte) -127,
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

		storage = new ExtendedStorage<>(entries -> {
			JsonArray array = new JsonArray();
			for (PlayerListEntry entry : entries)
				array.add(encode(entry));

			return array;
		}, elem -> {
			List<PlayerListEntry> entries = new ArrayList<>();
			for (JsonElement entry : elem.getAsJsonArray())
				entries.add(decode(entry));

			return entries;
		}, new ArrayList<>());

		EventRegisterer.register(this);
		init();
	}

	protected JsonElement encode(PlayerListEntry value) {
		return new JsonPrimitive(value.getId());
	}

	protected PlayerListEntry decode(JsonElement value) {
		return new PlayerListEntry(null, value.getAsString());
	}

	protected void edit(int editIndex, SettingContentActivity parent) {
		new PlayerListInputActivity(editIndex, parent).open();
	}

	protected void add(SettingContentActivity parent) {
		new PlayerListInputActivity(-1, parent).open();
	}

	protected String getName(PlayerListEntry entry) {
		return entry.name();
	}

	protected Icon getIcon(PlayerListEntry entry) {
		return Icon.head(entry.name());
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
	public ExtendedStorage<List<PlayerListEntry>> getStorage() {
		return storage;
	}

	/*
	TODO:
	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		List<V> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			V value = values.get(i);

			ButtonSettingImpl entry = new ButtonSettingImpl();
			entry.name(getName(value));
			entry.icon(getIcon(value));

			entry.setParent((Setting) this);

			int idx = i;
			event.settings().addChild(entry.createUnwrappedWidget(
				ButtonWidget.icon(
					Icons.of(Laby4Util.isVanillaTheme() ? "pencil_padded" : "high_res/pencil_vec"),
					() -> edit(idx, event.activity)
				).addId("delete-button"), // Actually an edit button, but id is required for styling

				ButtonWidget.icon(X, () -> {
					values.remove(idx);
					notifyChange();
					event.activity.reload();
				}).addId("delete-button")
			));
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> add(event.activity));
	}
	*/

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

			String defaultName = editIndex == -1 ? "" : get().get(editIndex).name();

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
					get().add(PlayerListEntryResolver.getEntry(name));
				else
					get().set(editIndex, PlayerListEntryResolver.getEntry(name));

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
			PlayerListEntry entry = PlayerListEntryResolver.getEntry(name);
			if (!entry.exists()) {
				textInput.textColor().set(0xFFFF0000);
				addButton.setEnabled(false);
			} else {
				textInput.textColor().set(0xFFFFFFFF);
				addButton.setEnabled(entry.loaded());
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
