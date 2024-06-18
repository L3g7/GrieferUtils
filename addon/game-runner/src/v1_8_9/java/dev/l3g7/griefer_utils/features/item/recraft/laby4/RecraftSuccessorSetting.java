/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.ButtonSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.FlexibleContentEntry;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@ExclusiveTo(LABY_4)
public class RecraftSuccessorSetting extends ListSetting implements BaseSettingImpl<RecraftSuccessorSetting, RecraftRecording> {

	private final ExtendedStorage<RecraftRecording> storage;
	private Runnable backButtonCb;

	public RecraftSuccessorSetting() {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {

				@Override
				public <T> T get() {
					return c(Collections.emptyList());
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{RecraftSuccessorSetting.class};}

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		this.storage = new ExtendedStorage<>(v -> {
			throw new UnsupportedOperationException();
		}, entries -> {
			throw new UnsupportedOperationException();
		}, null);
		EventRegisterer.register(this);

		callback(v -> {
			if (v != null) {
				name(v.name().get());
				icon(v.icon);
			} else {
				name("§7[Kein Nachfolger]");
				icon(Blocks.barrier);
			}
		});

		name("§7[Kein Nachfolger]");
		icon(Blocks.barrier);
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>();

		ButtonSettingImpl selectNothing = (ButtonSettingImpl) ButtonSetting.create()
			.name("Nichts auswählen")
			.icon(Blocks.barrier)
			.buttonLabel("Auswählen")
			.callback(() -> {
				RecraftSuccessorSetting.this.set(null);
				backButtonCb.run();
			});
		selectNothing.create(this);
		list.add(new KeyValue<>(selectNothing.getId(), selectNothing));

		for (RecraftPage page : Recraft.pages.get()) {
			RecraftSuccessorPage p = new RecraftSuccessorPage(page);
			p.create(this);

			list.add(new KeyValue<>(p.getId(), p));
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
	public ExtendedStorage<RecraftRecording> getStorage() {
		return storage;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		HorizontalListWidget header = event.get("setting-header");
		header.removeChild("add-button");

		ComponentWidget title = (ComponentWidget) header.getChild("title").childWidget();
		title.setComponent(Component.text("Nachfolgende Aufzeichnung auswählen..."));

		backButtonCb = header.getChild("back-button").childWidget()::onPress;

		// Update entry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ListSettingEntry) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						if (content.hashCode() != 0)
							return;

						// Fix icon
						IconWidget widget = new IconWidget(SettingsImpl.buildIcon(Items.map)); // NOTE: duplicate code
						widget.addId("setting-icon");
						content.addChild(0, new FlexibleContentEntry(widget, false));
						widget.initialize(content);

						// Update button icons
						ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
						btn.updateIcon(SettingsImpl.buildIcon("pencil_vec")); // NOTE: use original icons?
						content.removeChild("delete-button");
					}
				});
			}
		}
	}


	@ExclusiveTo(LABY_4)
	private class RecraftSuccessorPage extends ListSetting implements BaseSettingImpl<RecraftSuccessorPage, Object> {

		private final ExtendedStorage<Object> storage;
		private final RecraftPage page;

		public RecraftSuccessorPage(RecraftPage page) {
			this(new ExtendedStorage<>(v -> JsonNull.INSTANCE, entries -> NULL, NULL), page);
			EventRegisterer.register(this);
		}

		public RecraftSuccessorPage(ExtendedStorage<Object> storage, RecraftPage page) {
			super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
				new ConfigPropertySettingAccessor(null, null, null, null) {

					@Override
					public <T> T get() {
						return c(Collections.emptyList());
					}

					@Override
					public Type getGenericType() {
						return new ParameterizedType() {
							public Type[] getActualTypeArguments() {return new Type[]{RecraftSuccessorSetting.class};}

							public Type getRawType() {return null;}

							public Type getOwnerType() {return null;}
						};
					}
				}
			);

			this.storage = storage;
			this.page = page;
			this.name(page.name.get());
			this.icon(SettingsImpl.buildIcon(Items.map));
		}

		@Override
		public List<KeyValue<Setting>> getElements() {
			List<KeyValue<Setting>> list = new ArrayList<>();

			for (RecraftRecording recording : page.recordings.get()) {
				ButtonSettingImpl setting = (ButtonSettingImpl) ButtonSetting.create()
					.name(recording.name().get())
					.icon(recording.icon)
					.buttonLabel("Auswählen")
					.callback(() -> {
						RecraftSuccessorSetting.this.set(recording);
						backButtonCb.run();
						backButtonCb.run(); // Go pack 2 times
					});

				setting.create(this);
				list.add(new KeyValue<>(setting.getId(), setting));
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
		public ExtendedStorage<Object> getStorage() {
			return storage;
		}

		@EventListener
		private void onInit(SettingActivityInitEvent event) {
			if (event.holder() != this)
				return;

			// Update entry widgets
			HorizontalListWidget header = event.get("setting-header");
			header.removeChild("add-button");
			ComponentWidget title = (ComponentWidget) header.getChild("title").childWidget();
			title.setComponent(Component.text("Nachfolgende Aufzeichnung auswählen..."));

			for (Widget w : event.settings().getChildren()) {
				if (w instanceof SettingWidget s && s.setting() instanceof ListSettingEntry) {
					SettingsImpl.hookChildAdd(s, e -> {
						if (e.childWidget() instanceof FlexibleContentWidget content) {
							if (content.hashCode() != 0)
								return;

							// Fix icon
							IconWidget widget = new IconWidget(SettingsImpl.buildIcon(Items.map)); // NOTE: duplicate code; not required?
							widget.addId("setting-icon");
							content.addChild(0, new FlexibleContentEntry(widget, false));
							widget.initialize(content);

							// Update button icons
							ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
							btn.updateIcon(SettingsImpl.buildIcon("pencil_vec")); // NOTE: use original icons?
							content.removeChild("delete-button");
						}
					});
				}
			}
		}

	}
}