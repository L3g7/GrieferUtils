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
import dev.l3g7.griefer_utils.core.api.util.io.IO;
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
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class ListSettingImpl<E extends ListEntry<E>> extends ControlElement implements Laby3Setting<ListSetting<E>, Iterable<E>>, ListSetting<E> {

	private final ExtendedStorage<Iterable<E>> storage;
	private final E ctor;

	private Consumer<E> customEdit;

	private SettingsElement container = this;
	private final EntryAddSettingImpl addSetting = new EntryAddSettingImpl();

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
			int index = getSettings().indexOf(addSetting);
			DisplaySetting<E> setting = new DisplaySetting<>(ListSettingImpl.this, ctor.createNew());
			getSettings().add(index, setting);
			open(setting);
		});
	}

	@Override
	public ExtendedStorage<Iterable<E>> getStorage() {
		return storage;
	}

	@Override
	public void create(Object parent) {
		Laby3Setting.super.create(parent);
		this.container = (SettingsElement) parent;
		int index = getSettings().indexOf(this);
		getSettings().remove(this);

		List<SettingsElement> settings = new ArrayList<>();
		for (E entry : get())
			settings.add(new DisplaySetting<>(this, entry));

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
	public ListSetting<E> customEdit(dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer<E> callback) {
		customEdit = callback;
		return this;
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	private void open(DisplaySetting<E> setting) {
		if (customEdit != null)
			customEdit.accept(setting.data);
		else
			mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(this::notifyChange, setting));
	}

	private static class DisplaySetting<E extends ListEntry<E>> extends ListEntrySetting {

		private final ListSettingImpl<E> parent;
		private final E data;

		public DisplaySetting(ListSettingImpl<E> parent, E entry) {
			super(true, true, false);
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

			name(data.getName());
			List<SettingsElement> settings = getSubSettings().getElements();
			settings.clear();
			settings.addAll(c(data.toSettings()));
		}

		@Override
		protected void onChange() {
			((ArrayList<E>) parent.get()).remove(data);
			parent.getSettings().remove(this);
			parent.notifyChange();
		}

		@Override
		protected void openSettings() {
			parent.open(this);
		}

	}

}
