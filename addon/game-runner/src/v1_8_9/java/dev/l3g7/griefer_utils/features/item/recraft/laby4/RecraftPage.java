/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.StringSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.SettingPermissionHolder;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.features.item.recraft.laby4.RecraftBridgeImpl.pages;

public class RecraftPage extends net.labymod.api.configuration.loader.Config implements ListSettingConfig {

	public final StringSettingImpl name = (StringSettingImpl) StringSetting.create()
		.name("Name")
		.description("Wie diese Seite heißen soll.")
		.icon("name_tag")
		.callback(s -> pages.notifyChange());

	public final RecraftRecording.RecraftRecordingListSetting recordings = new RecraftRecording.RecraftRecordingListSetting()
		.name("Aufzeichnungen")
		.icon("camera")
		.callback(pages::notifyChange);

	public RecraftPage(String name) {
		this.name.set(name);
	}

	public void create(Object parent) {
		name.create(parent);
		recordings.create(parent);
	}

	@Override
	public boolean isInvalid() {
		return name.get().isBlank();
	}

	@Override
	public @NotNull Component entryDisplayName() {
		return Component.text(name.get());
	}

	@Override
	public @NotNull List<Setting> toSettings(@Nullable Setting parent, SpriteTexture texture) {
		return Arrays.asList(name, recordings);
	}

	// NOTE: cleanup? merge?
	@ExclusiveTo(LABY_4)
	public static class RecraftPageListSetting extends ListSetting implements Laby4Setting<RecraftPageListSetting, List<RecraftPage>> {

		private final ExtendedStorage<List<RecraftPage>> storage;

		public RecraftPageListSetting() {
			this(new ExtendedStorage<>(v -> {
				JsonArray pages = new JsonArray();
				for (RecraftPage entry : v) {
					JsonObject obj = new JsonObject();
					obj.addProperty("name", entry.name.get()); // NOTE: GSON?
					obj.add("recordings", entry.recordings.getStorage().encodeFunc.apply(entry.recordings.get()));
					pages.add(obj);
				}
				return pages;
			}, entries -> {
				List<RecraftPage> v = new ArrayList<>();
				for (JsonElement elem : entries.getAsJsonArray()) {
					JsonObject page = elem.getAsJsonObject();
					RecraftPage cfg = new RecraftPage(page.get("name").getAsString());
					cfg.recordings.set(cfg.recordings.getStorage().decodeFunc.apply(page.get("recordings")));
					v.add(cfg);
				}
				return v;
			}, new ArrayList<>()));
		}

		public RecraftPageListSetting(ExtendedStorage<List<RecraftPage>> storage) {
			super(UUID.randomUUID().toString(), null, null, new String[0], (SettingPermissionHolder) null, null, (byte) -127,
				new ConfigPropertySettingAccessor(null, null, null, null) {
					@Override
					public <T> T get() {
						return c(storage.value == null ? storage.fallbackValue : storage.value);
					}

					@Override
					public <T> void set(T value) {
						List<RecraftPage> list = get();
						ArrayList<RecraftPage> val = c(value);
						list.clear();
						list.addAll(val);
					}

					@Override
					public Type getGenericType() {
						return new ParameterizedType() {
							public Type[] getActualTypeArguments() {return new Type[]{RecraftPage.class};}

							public Type getRawType() {return null;}

							public Type getOwnerType() {return null;}
						};
					}
				}
			);

			this.storage = storage;
		}

		@Override
		public ListSettingEntry createNew() {
			RecraftPage config = new RecraftPage("Seite " + (get().size() + 1));
			get().add(config);

			ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), get().size()) {
				public Icon getIcon() {
					return Icons.of("command_suggestions");
				}
			};

			entry.addSettings(config);
			config.create(entry);
			return entry;
		}

		@Override
		public List<KeyValue<Setting>> getElements() {
			List<KeyValue<Setting>> list = new ArrayList<>();

			List<RecraftPage> entries = get();

			for (int i = 0; i < entries.size(); ++i) {
				RecraftPage config = entries.get(i);
				if (config.isInvalid()) {
					entries.remove(i--);
				} else {
					config.create(this);
					ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), i) {
						@Override
						public Icon getIcon() {
							return Icons.of("command_suggestions");
						}
					};
					entry.addSettings(config);
					list.add(new KeyValue<>(entry.getId(), entry));
				}
			}

			return list;
		}

		@Override
		public void remove(ListSettingEntry entry) {
			get().remove(entry.listIndex());
			notifyChange();
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
		public ExtendedStorage<List<RecraftPage>> getStorage() {
			return storage;
		}

	}
}
