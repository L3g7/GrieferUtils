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
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.temp.TempSettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
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

@ExclusiveTo(LABY_4)
public class EntryListSetting extends ListSetting implements Laby4Setting<EntryListSetting, List<EntryConfig>> {

	final ExtendedStorage<List<EntryConfig>> storage;

	public EntryListSetting() {
		this(new ExtendedStorage<>(v -> {
			JsonArray entries = new JsonArray();
			for (EntryConfig entry : v) {
				JsonObject obj = new JsonObject();
				obj.addProperty("name", entry.name.get());
				obj.addProperty("command", entry.command.get());
				obj.addProperty("cb", entry.citybuild.get().getName()); // NOTE GSON?
				entries.add(obj);
			}
			return entries;
		}, entries -> {
			List<EntryConfig> v = new ArrayList<>();
			for (JsonElement elem : entries.getAsJsonArray()) {
				JsonObject obj = elem.getAsJsonObject();
				EntryConfig entry = new EntryConfig();
				entry.name.set(obj.get("name").getAsString());
				entry.command.set(obj.get("command").getAsString());
				entry.citybuild.set(Citybuild.getCitybuild(obj.get("cb").getAsString()));
				v.add(entry);
			}
			return v;
		}, new ArrayList<>()));
		EventRegisterer.register(this);
	}

	private EntryListSetting(ExtendedStorage<List<EntryConfig>> storage) {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(storage.value == null ? storage.fallbackValue : storage.value);
				}

				@Override
				public <T> void set(T value) {
					List<EntryConfig> list = get();
					ArrayList<EntryConfig> val = c(value);
					list.clear();
					list.addAll(val);
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{EntryConfig.class};}

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);
		this.storage = storage;
		for (EntryConfig entry : get())
			entry.create(this);
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>();

		List<EntryConfig> configs = get();
		for (int i = 0; i < configs.size(); ++i) {
			EntryConfig config = configs.get(i);
			if (config.isInvalid()) {
				configs.remove(i--);
				notifyChange();
			} else {
				ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), i);
				entry.addSettings(config);
				config.create(this);
				list.add(new KeyValue<>(entry.getId(), entry));
			}
		}

		return list;
	}

	@Override
	public ListSettingEntry createNew() {
		EntryConfig config = new EntryConfig();
		config.create(this);
		get().add(config);
		notifyChange();

		ListSettingEntry entry = new ListSettingEntry(this, config.newEntryTitle(), get().size() - 1) {
			public Icon getIcon() {
				return Icons.of(Items.map);
			}
		};

		entry.addSettings(config);
		return entry;
	}

	@EventListener
	private void onInit(TempSettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		// Update entry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ListSettingEntry entry) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						// Fix icon
						IconWidget widget = new IconWidget(Icons.of("command_pie_menu"));
						widget.addId("setting-icon");
						content.addChild(0, new FlexibleContentEntry(widget, false));
						widget.initialize(content);

						// Update button icons
						ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
						btn.updateIcon(Icons.of("pencil_vec"));
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
	public ExtendedStorage<List<EntryConfig>> getStorage() {
		return storage;
	}
}
