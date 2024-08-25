/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value.laby4;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.ItemStackIcon;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.navigation.elements.ScreenNavigationElement;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.ScreenRendererWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.KeyValue;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.WidgetsEditorActivity;
import net.labymod.core.client.gui.screen.widget.widgets.hud.window.HudWidgetWindowWidget;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.labymod.api.Textures.SpriteCommon.X;

@ExclusiveTo(LABY_4)
public class ItemValueListSetting extends ListSetting implements BaseSettingImpl<ItemValueListSetting, List<ItemValue>> {

	private final ExtendedStorage<List<ItemValue>> storage;

	/**
	 * The screen that triggered the current item add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	public ItemValueListSetting() {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(new ArrayList<>());
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{Void.class};} // NOTE: cleanup / generalize

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		storage = new ExtendedStorage<>(list -> {
			JsonObject obj = new JsonObject();
			for (ItemValue item : list)
				obj.addProperty(ItemUtil.serializeNBT(item.stack), item.value); // NOTE: move serialization to ItemValue; GSON?; update keys

			return obj;
		}, elem -> {
			List<ItemValue> list = new ArrayList<>();
			for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet())
				list.add(new ItemValue(ItemUtil.fromNBT(entry.getKey()), entry.getValue().getAsInt()));

			return list;
		}, new ArrayList<>());

		EventRegisterer.register(this);
	}

	@Override
	public ExtendedStorage<List<ItemValue>> getStorage() {
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
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>();

		List<ItemValue> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			ItemValue value = values.get(i);

			ItemValueEntry entry = new ItemValueEntry(value, i);
			entry.name(value.stack.getDisplayName(),
					"§f§o➡ " + Constants.DECIMAL_FORMAT_98.format(value.value) + "$")
				.icon(value.stack);

			entry.getChildSettings().forEach(s -> ((AbstractSetting<?, ?>) s).callback(this::notifyChange));

			entry.create(this);
			list.add(new KeyValue<>(entry.getId(), entry));
		}

		return list;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		// Update entry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ItemValueEntry entry) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
						btn.updateIcon(SettingsImpl.buildIcon("pencil_vec"));

						content.addContent(ButtonWidget.icon(X, () -> {
							get().remove(entry.index);
							notifyChange();
							event.activity.reload();
						}).addId("delete-button"));
					}
				});
			}
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> {
			if (mc().thePlayer == null) {
				LabyBridge.labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur Ingame möglich!", 5000);
				return;
			}

			previousScreen = mc().currentScreen;
			display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
			mc().displayGuiScreen(null);
		});

	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		get().add(new ItemValue(event.itemStack, 0));
		notifyChange();
		previousScreen = null;
		event.cancel();

		// Check if in widgets activity
		if (!(Laby4Util.getActivity() instanceof NavigationActivity navActivity))
			return;

		ScreenNavigationElement element = Reflection.get(navActivity, "element");
		if (!(element instanceof LabyModNavigationElement))
			return;

		LabyModActivity activity = (LabyModActivity) element.getScreen();
		if (activity == null)
			return;

		if (activity.getById("widgets") != activity.getActiveTab())
			return;

		// Check if in this setting
		ScreenInstance instance = Reflection.get(activity.getActiveTab(), "instance");
		WidgetsEditorActivity settingsActivity = (WidgetsEditorActivity) instance;

		HudWidgetWindowWidget w = settingsActivity.window();
		ScreenRendererWidget screenRendererWidget = Reflection.get(w, "contentRendererWidget");
		ScreenInstance screen = screenRendererWidget.getScreen();
		if (!(screen instanceof SettingContentActivity settingContentActivity))
			return;

		if (settingContentActivity.getCurrentHolder() != this)
			return;

		// Open new item
		Function<Setting, Setting> screenCallback = Reflection.get(settingContentActivity, "screenCallback");

		List<KeyValue<Setting>> values = getElements();
		Reflection.set(settingContentActivity, "currentHolder", values.get(values.size() - 1).getValue());
		if (screenCallback != null)
			Reflection.set(settingContentActivity, "currentHolder", screenCallback.apply(settingContentActivity.getCurrentHolder()));

		if (settingContentActivity.getCurrentHolder() != null)
			settingContentActivity.reload();
	}

	private static class ItemValueEntry extends AbstractSettingImpl<ItemValueEntry, Object> implements AbstractSetting<ItemValueEntry, Object> {

		private final int index;
		private final ItemValue value;

		public ItemValueEntry(ItemValue value, int index) {
			super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
			this.value = value;
			this.index = index;

			subSettings(
				NumberSetting.create() // NOTE: add $ prefix?
					.name("Wert")
					.icon("coin_pile")
					.defaultValue(value.value)
					.callback(v -> value.value = v)
			);
		}

		@Override
		protected Widget[] createWidgets() {
			return null;
		}

		@Override
		public Component displayName() {
			// Use stripped name for settings
			if ("initialize".equals(new Throwable().getStackTrace()[1].getMethodName())) // TODO find better way of detection
				return Component.text(value.stack.getDisplayName());

			return super.displayName();
		}

		@Override
		public Icon getIcon() {
			if ("initialize".equals(new Throwable().getStackTrace()[1].getMethodName()))
				return new ItemStackIcon(c(value.stack), 2, -3, 1);

			return super.getIcon();
		}
	}

}
