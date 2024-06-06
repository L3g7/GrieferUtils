/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.command_pie_menu.laby4;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.FlexibleContentEntry;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.minecraft.init.Items;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static net.labymod.api.Textures.SpriteCommon.X;

// NOTE: cleanup? merge?

@Bridge
@ExclusiveTo(LABY_4)
public class PageListSettingImpl extends ListSetting implements PageListSetting, BaseSettingImpl<PageListSetting, List<Page>> {

	private final ExtendedStorage<List<Page>> storage;

	public PageListSettingImpl() {
		this(new ExtendedStorage<>(v -> {
			JsonArray pages = new JsonArray();
			for (PageConfig entry : v) {
				JsonObject obj = new JsonObject();
				obj.addProperty("name", entry.name.get());
				obj.add("entries", entry.entries.storage.encodeFunc.apply(entry.entries.get()));
				pages.add(obj);
			}
			return pages;
		}, entries -> {
			List<PageConfig> v = new ArrayList<>();
			for (JsonElement elem : entries.getAsJsonArray()) {
				JsonObject page = elem.getAsJsonObject();
				PageConfig cfg = new PageConfig(page.get("name").getAsString());
				cfg.entries.set(cfg.entries.storage.decodeFunc.apply(page.get("entries")));
				v.add(cfg);
			}
			return v;
		}, new ArrayList<>()));
		EventRegisterer.register(this);
	}

	private PageListSettingImpl(ExtendedStorage<List<PageConfig>> storage) {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(storage.value == null ? storage.fallbackValue : storage.value);
				}

				@Override
				public <T> void set(T value) {
					List<PageConfig> list = get();
					ArrayList<PageConfig> val = c(value);
					list.clear();
					list.addAll(val);
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{PageConfig.class};}

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		this.storage = c(storage);
		for (PageConfig page : getTyped())
			page.create(this);
	}

	private List<PageConfig> getTyped() {
		return c(get());
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>();

		List<PageConfig> configs = getTyped();
		for (int i = 0; i < configs.size(); ++i) {
			PageConfig config = configs.get(i);
			if (config.isInvalid()) {
				configs.remove(i--);
				notifyChange();
			} else {
				ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), i);
				entry.addSettings(config);
				list.add(new KeyValue<>(entry.getId(), entry));
			}
		}

		return list;
	}

	@Override
	public ListSettingEntry createNew() {
		PageConfig config = new PageConfig("Seite " + (get().size() + 1));
		config.create(this);
		get().add(config);
		notifyChange();

		ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), get().size() - 1);
		entry.addSettings(config);
		return entry;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		// Update entry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ListSettingEntry entry) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						// Fix icon
						IconWidget widget = new IconWidget(SettingsImpl.buildIcon(Items.map));
						widget.addId("setting-icon");
						content.addChild(0, new FlexibleContentEntry(widget, false));
						widget.initialize(content);

						// Update button icons
						ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
						btn.updateIcon(SettingsImpl.buildIcon("pencil_vec")); // NOTE: use original icons?
						content.removeChild("delete-button");

						content.addContent(ButtonWidget.icon(X, () -> {
							get().remove(entry.listIndex());
							notifyChange();
							event.activity.reload();
						}).addId("delete-button"));
					}
				});
			}
		}
	}

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Icon getIcon() {
		return storage.icon;
	}

	@Override
	public ExtendedStorage<List<Page>> getStorage() {
		return storage;
	}

}
