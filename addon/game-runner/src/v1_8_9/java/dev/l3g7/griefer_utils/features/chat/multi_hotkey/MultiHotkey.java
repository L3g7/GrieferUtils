/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.multi_hotkey;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.temp.TempSettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.StringSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.FlexibleContentEntry;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.minecraft.init.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.labymod.api.Textures.SpriteCommon.X;

@Singleton
@ExclusiveTo(LABY_4)
public class MultiHotkey extends Feature {

	private final HotkeyListSetting entries = new HotkeyListSetting()
		.name("Hotkeys")
		.icon("labymod_3/autotext");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Multi-Hotkey")
		.description("Erlaubt das Ausführen von mehreren sequenziellen Befehlen auf Tastendruck.")
		.icon("labymod_3/autotext")
		.subSettings(entries);

	@EventListener
	public void onKeyPress(KeyInputEvent event) {
		if (Keyboard.isRepeatEvent())
			return;

		for (HotkeyConfig hotkey : entries.get()) {
			Set<Integer> keys = hotkey.key.get();
			if (!keys.contains(Keyboard.getEventKey()))
				continue;

			if (!keys.stream().allMatch(i -> Key.get(i).isPressed()))
				continue;

			if (!hotkey.citybuild.get().isOnCb())
				continue;

			String command = hotkey.commands.get().get(hotkey.amountsTriggered %= hotkey.commands.get().size());
			if (!MessageEvent.MessageSendEvent.post(command))
				player().sendChatMessage(command);

			hotkey.amountsTriggered++;

		}
	}

	@ExclusiveTo(LABY_4)
	private class HotkeyConfig extends net.labymod.api.configuration.loader.Config implements ListSettingConfig {

		private int amountsTriggered = 0;

		private final StringSettingImpl name = (StringSettingImpl) StringSetting.create()
			.name("Name")
			.description("Wie dieser Hotkey heißen soll.")
			.icon(Items.writable_book)
			.callback(entries::notifyChange);

		private final KeySetting key = KeySetting.create()
			.name("Taste")
			.description("Durch das Drücken welcher Taste/-n dieser Hotkey ausgelöst werden soll.")
			.icon("key")
			.callback(entries::notifyChange);

		private final CitybuildSetting citybuild = CitybuildSetting.create()
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Hotkey funktionieren soll.")
			.callback(entries::notifyChange);

		private final StringListSetting commands = StringListSetting.create()
			.name("Befehle")
			.icon(Items.paper)
			.entryIcon(Items.paper)
			.callback(entries::notifyChange);

		public HotkeyConfig(String name) {
			this.name.set(name);
		}

		public void create(Object parent) {
			name.create(parent);
			key.create(parent);
			citybuild.create(parent);
			commands.create(parent);
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
			return Arrays.asList(name, (Setting) key, (Setting) citybuild, (Setting) commands);
		}
	}

	// NOTE: cleanup? merge?
	@ExclusiveTo(LABY_4)
	private class HotkeyListSetting extends ListSetting implements Laby4Setting<HotkeyListSetting, List<HotkeyConfig>> {

		private final ExtendedStorage<List<HotkeyConfig>> storage;

		public HotkeyListSetting() {
			this(new ExtendedStorage<>(v -> {
				JsonArray pages = new JsonArray();
				for (HotkeyConfig entry : v) {
					JsonObject obj = new JsonObject();
					obj.addProperty("name", entry.name.get());
					obj.add("keys", entry.key.getStorage().encodeFunc.apply(entry.key.get()));
					obj.add("commands", entry.commands.getStorage().encodeFunc.apply(entry.commands.get()));
					obj.add("cb", entry.citybuild.getStorage().encodeFunc.apply(entry.citybuild.get())); // NOTE: GSON?
					pages.add(obj);
				}
				return pages;
			}, entries -> {
				List<HotkeyConfig> v = new ArrayList<>();
				for (JsonElement elem : entries.getAsJsonArray()) {
					JsonObject page = elem.getAsJsonObject();
					HotkeyConfig cfg = new HotkeyConfig(page.get("name").getAsString());
					cfg.key.set(cfg.key.getStorage().decodeFunc.apply(page.get("keys")));
					cfg.commands.set(cfg.commands.getStorage().decodeFunc.apply(page.get("commands")));
					cfg.citybuild.set(cfg.citybuild.getStorage().decodeFunc.apply(page.get("cb")));
					v.add(cfg);
				}
				return v;
			}, new ArrayList<>()));
			EventRegisterer.register(this);
		}

		public HotkeyListSetting(ExtendedStorage<List<HotkeyConfig>> storage) {
			super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
				new ConfigPropertySettingAccessor(null, null, null, null) {
					@Override
					public <T> T get() {
						return c(storage.value == null ? storage.fallbackValue : storage.value);
					}

					@Override
					public <T> void set(T value) {
						List<HotkeyConfig> list = get();
						ArrayList<HotkeyConfig> val = c(value);
						list.clear();
						list.addAll(val);
					}

					@Override
					public Type getGenericType() {
						return new ParameterizedType() {
							public Type[] getActualTypeArguments() {return new Type[]{HotkeyConfig.class};}

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
			HotkeyConfig config = new HotkeyConfig("Neuer Hotkey");
			get().add(config);

			ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), get().size()) {
				public Icon getIcon() {
					return Icons.of("labymod_3/autotext");
				}
			};

			entry.addSettings(config);
			config.create(entry);
			return entry;
		}

		@Override
		public List<KeyValue<Setting>> getElements() {
			List<KeyValue<Setting>> list = new ArrayList<>();

			List<HotkeyConfig> entries = get();

			for (int i = 0; i < entries.size(); ++i) {
				HotkeyConfig config = entries.get(i);
				if (config.isInvalid()) {
					entries.remove(i--);
				} else {
					config.create(this);
					ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), i);
					entry.addSettings(config);
					list.add(new KeyValue<>(entry.getId(), entry));
				}
			}

			return list;
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
		public ExtendedStorage<List<HotkeyConfig>> getStorage() {
			return storage;
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
							IconWidget widget = new IconWidget(Icons.of("labymod_3/autotext")); // NOTE: duplicate code; use LM4's icon?
							widget.addId("setting-icon");
							content.addChild(0, new FlexibleContentEntry(widget, false));
							widget.initialize(content);

							// Update button icons
							ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
							btn.updateIcon(Icons.of("pencil_vec")); // NOTE: use original icons?
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

	}

}
