/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.EntryAddSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.ListEntrySetting;
import dev.l3g7.griefer_utils.labymod.laby3.util.AddonsGuiWithCustomBackButton;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class ListSettingImpl<E extends ListEntry<E>> extends ControlElement implements Laby3Setting<ListSetting<E>, List<E>>, ListSetting<E> {

	private final ExtendedStorage<List<E>> storage;
	private final E ctor;

	private boolean unpacked = false;
	private Consumer<E> customEdit;

	protected SettingsElement container = this;
	protected final EntryAddSettingImpl addSetting = new EntryAddSettingImpl();
	private boolean created = false;

	public ListSettingImpl(Class<E> type) {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
		this.ctor = IO.GSON.fromJson(new JsonObject(), type);

		storage = new ExtendedStorage<>(e -> {
			JsonArray data = new JsonArray();
			for (E element : e)
				data.add(element.encode());

			return data;
		}, e -> {
			List<E> list = new ArrayList<>();
			for (JsonElement element : e.getAsJsonArray()) {
				E entry = ctor.createNew();
				entry.load(element);
				list.add(entry);
			}

			return list;
		}, new ArrayList<>());
		storage.unsetIfDefaultValue = false;

		addSetting.name("Eintrag hinzufügen");
		addSetting.callback(() -> {
			if (customEdit != null) {
				customEdit.accept(null);
			} else {
				int index = getSettings().indexOf(addSetting);
				DisplaySetting<E> setting = createDisplaySetting(ctor.createNew());
				getSettings().add(index, setting);
				mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(this::notifyChange, setting));
			}
		});
	}

	@Override
	public ExtendedStorage<List<E>> getStorage() {
		return storage;
	}

	protected static SettingsElement pack(ControlElement self, Object parent) {
		Laby3Setting<?, ?> setting = (Laby3Setting<?, ?>) self;

		// Wrap in CategorySetting
		List<SettingsElement> elements = ((SettingsElement) parent).getSubSettings().getElements();
		int idx = elements.indexOf(self);
		elements.remove(self);

		CategorySetting wrapper = CategorySetting.create()
			.name(self.getDisplayName())
			.description(self.getDescriptionText())
			.subSettings(setting);

		Reflection.set(wrapper, "iconData", self.getIconData());
		elements.add(idx, (SettingsElement) wrapper);

		wrapper.create(parent);
		setting.create(wrapper);
		return (SettingsElement) wrapper;
	}

	@Override
	public void create(Object parent) {
		if (created)
			return;

		created = true;
		if (!unpacked) {
			unpacked = true;
			this.container = pack(this, parent);
		} else {
			Laby3Setting.super.create(parent);
			this.container = (SettingsElement) parent;
		}

		int index = getSettings().indexOf(this);
		getSettings().remove(this);

		List<SettingsElement> settings = new ArrayList<>();
		for (E entry : get())
			settings.add(createDisplaySetting(entry));

		settings.add(addSetting);
		getSettings().addAll(index, settings);

		callback(() -> {
			// Rebuild settings
			for (SettingsElement setting : getSettings())
				if (setting instanceof DisplaySetting<?> ds)
					ds.build();
		});
	}

	@Override
	public void add(E value) {
		int index = getSettings().indexOf(addSetting);
		getSettings().add(index, createDisplaySetting(value));
		get().add(value);
	}

	@Override
	public ListSetting<E> unpacked() {
		this.unpacked = true;
		return this;
	}

	@Override
	public ListSetting<E> customEdit(Consumer<E> callback) {
		customEdit = callback;
		return this;
	}

	protected List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	protected DisplaySetting<E> createDisplaySetting(E entry) {
		return new DisplaySetting<>(this, entry);
	}

	protected static class DisplaySetting<E extends ListEntry<E>> extends ListEntrySetting {

		private final ListSettingImpl<E> parent;
		protected final E data;

		public DisplaySetting(ListSettingImpl<E> parent, E entry) {
			super(true, true, false);
			this.container = parent.container;
			this.parent = parent;
			data = entry;
			build();
		}

		public void build() {
			String resourceIcon = data.resourceIcon();
			if (resourceIcon != null)
				icon(resourceIcon);

			ItemStack itemIcon = data.itemIcon();
			if (itemIcon != null)
				icon(itemIcon);

			String subtext = data.subtextLaby3();
			if (subtext != null)
				name(data.getName(), subtext);
			else
				name(data.getName());

			if (parent.customEdit == null) {
				List<SettingsElement> settings = getSubSettings().getElements();
				settings.addAll(c(data.toSettings()));
			}
		}

		@Override
		protected void onChange() {
			parent.get().remove(data);
			parent.getSettings().remove(this);
			parent.notifyChange();
		}

		@Override
		protected void openSettings() {
			if (parent.customEdit != null)
				parent.customEdit.accept(data);
			else
				mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(this::notifyChange, this));
		}

	}

}
