/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.ItemProtection.ProtectionType;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.KeyValue;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.ItemProtection.ProtectionType.*;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.labymod.api.Textures.SpriteCommon.X;

public class ItemProtectionListSetting extends ListSetting implements BaseSettingImpl<ItemProtectionListSetting, List<ItemProtection>> {

	private final ExtendedStorage<List<ItemProtection>> storage;

	/**
	 * The screen that triggered the current item add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	public ItemProtectionListSetting() {
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
			for (ItemProtection item : list) {
				JsonObject entry = new JsonObject();
				entry.addProperty("name", item.name); // NOTE: move serialization to ItemProtection; GSON?; update keys

				for (ProtectionType type : ProtectionType.values())
					entry.addProperty(type.configKey, item.isProtectedAgainst(type));

				obj.add(ItemUtil.serializeNBT(item.stack), entry);
			}
			return obj;
		}, elem -> {
			List<ItemProtection> list = new ArrayList<>();
			for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet()) {
				ItemProtection item = new ItemProtection(ItemUtil.fromNBT(entry.getKey()));
				JsonObject data = entry.getValue().getAsJsonObject();

				item.name = data.get("name").getAsString();

				for (ProtectionType type : ProtectionType.values())
					item.states[type.ordinal()] = (data.get(type.configKey).getAsBoolean());

				list.add(item);
			}
			return list;
		}, new ArrayList<>(Arrays.asList(ItemProtection.BONZE, ItemProtection.BIRTH)));

		EventRegisterer.register(this);
	}

	@Override
	public ExtendedStorage<List<ItemProtection>> getStorage() {
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

		List<ItemProtection> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			ItemProtection protection = values.get(i);

			ItemProtectionEntry entry = new ItemProtectionEntry(protection, i);
			entry.name(protection.name)
				.icon(protection.stack);

			entry.getSubSettings().forEach(s -> ((AbstractSetting<?, ?>) s).callback(this::notifyChange));

			entry.create(this);
			list.add(new KeyValue<>(entry.getId(), entry));
		}

		return list;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		// Update ProtectedItemSettingEntry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ItemProtectionEntry entry) {
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
		get().add(new ItemProtection(event.itemStack));
		notifyChange();
		previousScreen = null;
		event.cancel();
	}

	private static class ItemProtectionEntry extends AbstractSettingImpl<ItemProtectionEntry, Object> implements AbstractSetting<ItemProtectionEntry, Object> {

		private final int index;

		public ItemProtectionEntry(ItemProtection protection, int index) { // NOTE: probably creating a lot of mem leaks currently due to not unregistering event listeners
			super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
			this.index = index;
			subSettings(StringSetting.create(), SwitchSetting.create());

			StringSetting name = StringSetting.create() // NOTE: update name live
				.name("Anzeigename")
				.description("Der Anzeigename des Eintrags. Hat keinen Einfluss auf die geretten Items.")
				.icon(Items.writable_book)
				.defaultValue(protection.name)
				.callback(s -> protection.name = s);

			SwitchSetting drop = DROP.createSetting(protection);
			SwitchSetting itemPickup = ITEM_PICKUP.createSetting(protection);

			itemPickup.callback(b -> { if (b) drop.set(true); });
			drop.callback(b -> { if (!b) itemPickup.set(false); });

			subSettings(name, drop, itemPickup, LEFT_CLICK.createSetting(protection), RIGHT_CLICK.createSetting(protection));
		}

		@Override
		protected Widget[] createWidgets() {
			return null;
		}

	}

}
