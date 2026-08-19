/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types.list;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.ActivityInitializeEvent.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsSettingWidget;

import java.util.ArrayList;
import java.util.List;

public class WrappingListSetting<E extends ListEntry<E>> extends ListSettingImpl<E> {

	private final List<KeyValue<Setting>> subsettings = new ArrayList<>();
	private final AbstractSettingImpl<?, ?> inner;

	public WrappingListSetting(ListSettingImpl<E> original, Setting inner) {
		super(original);
		if (!(inner instanceof SwitchSettingImpl ssi))
			// Other types probably work, but should be checked if added
			throw new UnsupportedOperationException("Wrapping " + inner.getClass() + " is unsupported!");

		this.inner = ssi;
	}

	@Override
	public Component displayName() {
		return inner.displayName();
	}

	@Override
	public Component getDescription() {
		return inner.getDescription();
	}

	@Override
	public Icon getIcon() {
		return inner.getIcon();
	}

	@Override
	public void create(Object parent) {
		super.create(parent);
		this.setRevision(inner.getRevision());

		if (getWidgets() == null) {
			inner.create(parent);
			setWidgets(inner.getWidgets());
		}

		for (KeyValue<Setting> kv : inner.getElements()) {
			WrappingListSettingEntry setting = new WrappingListSettingEntry(this, kv.getValue());
			subsettings.add(new KeyValue<>(kv.getKey(), setting));
		}
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>(subsettings.size() + rawList.size());
		list.addAll(subsettings);
		insertElements(list);
		return list;
	}

	/**
	 * Fake list entry that wraps a setting.
	 */
	private static class WrappingListSettingEntry extends ListSettingEntry {

		private final Setting inner;

		public WrappingListSettingEntry(ListSettingImpl<?> parent, Setting inner) {
			super(parent, inner.displayName(), null, -1);
			this.inner = inner;

			if (inner instanceof SettingElement se)
				setWidgets(se.getWidgets());

			addSettings(inner.values());
		}

		@Override
		public Component getDescription() {
			return inner.getDescription();
		}

		@Override
		public Icon getIcon() {
			return inner.getIcon();
		}

		@Override
		public boolean hasAdvancedButton() {
			return !this.getElements().isEmpty();
		}

		@Override
		public int listIndex() {
			throw new UnsupportedOperationException("Cannot get index of wrapped entry!");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove wrapped entry!");
		}

		@EventListener
		private static void onInit(SettingActivityInitEvent event) {
			if (!(event.parent() instanceof WrappingListSetting<?>))
				return;

			for (Widget child : event.settings().getChildren()) {
				if (child instanceof ModsSettingWidget setting) {
					SettingElement element = Reflection.get(setting, "element");
					if (element instanceof WrappingListSettingEntry)
						if (Laby4Util.get(setting, "mods-setting-content", "mods-setting-control") instanceof HorizontalListWidget buttons)
							buttons.removeChild("mods-setting-delete");
				}
			}
		}

	}

}
