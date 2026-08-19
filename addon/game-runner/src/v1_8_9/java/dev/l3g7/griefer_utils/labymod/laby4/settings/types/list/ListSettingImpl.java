/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.ActivityInitializeEvent.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.SettingPermissionHolder;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class ListSettingImpl<E extends ListEntry<E>> extends net.labymod.api.configuration.settings.type.list.ListSetting implements Laby4Setting<ListSetting<E>, Iterable<E>>, ListSetting<E> {

	private final ExtendedStorage<Iterable<E>> storage;
	protected final List<EntryConfig<E>> rawList;
	protected final Iterable<E> view = Itr::new;
	protected final E ctor;

	protected boolean unpacked = false;
	protected Consumer<E> customEdit;

	public ListSettingImpl(Class<E> type) {
		this(type, new SettingAccessor<>());
	}

	private ListSettingImpl(Class<E> type, SettingAccessor accessor) {
		super(UUID.randomUUID().toString(), null, null, new String[0], (SettingPermissionHolder) null, null, (byte) -127, accessor);

		this.rawList = c(accessor.list);
		this.ctor = IO.GSON.fromJson(new JsonObject(), type);

		storage = new ExtendedStorage<>(e -> {
			JsonArray data = new JsonArray();
			for (E element : e)
				data.add(element.encode());

			return data;
		}, e -> {
			rawList.clear();
			for (JsonElement element : e.getAsJsonArray()) {
				E entry = ctor.createNew();
				entry.load(element);
				rawList.add(new EntryConfig<>(this, entry));
			}

			return view;
		}, view);
		storage.unsetIfDefaultValue = false;

		init();
		setAccessor(accessor);
	}

	protected ListSettingImpl(ListSettingImpl<E> original) {
		super(UUID.randomUUID().toString(), null, null, new String[0], (SettingPermissionHolder) null, null, (byte) -127, original.getAccessor());
		this.storage = original.storage;
		this.rawList = original.rawList;
		this.ctor = original.ctor;
		this.unpacked = false;
		this.customEdit = original.customEdit;
	}

	@Override
	public ExtendedStorage<Iterable<E>> getStorage() {
		return storage;
	}

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Component getDescription() {
		String description = getStorage().description;
		return description == null ? null : Component.text(description);
	}

	@Override
	public Icon getIcon() {
		return getStorage().icon;
	}

	@Override
	public ListSetting<E> unpacked() {
		this.unpacked = true;
		return this;
	}

	@Override
	public ListSetting<E> customEdit(dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer<E> callback) {
		this.customEdit = callback;
		return this;
	}

	@Override
	public ListSetting<E> set(Iterable<E> value) {
		if (value != view)
			throw new UnsupportedOperationException();

		return this;
	}

	@Override
	public void create(Object parent) {
		Laby4Setting.super.create(parent);

		if (unpacked) {
			Setting sParent = (Setting) parent;
			Setting parentHolder = sParent.parent();

			// Remove self from parent
			sParent.getElements().remove(sParent.getElementById(getId()));

			// Replace parent of this setting with WrappingListSetting
			List<KeyValue<Setting>> subsettings = parentHolder.getElements();
			KeyValue<Setting> kv = parentHolder.getElementById(sParent.getId());
			Objects.requireNonNull(kv);

			WrappingListSetting<E> wrappedParent = new WrappingListSetting<>(this, sParent);
			wrappedParent.create(parentHolder);

			subsettings.set(subsettings.indexOf(kv),
				new KeyValue<>(kv.getKey(), wrappedParent));
		}
	}

	@Override
	public ListSettingEntry createNew() {
		return createNew(false);
	}

	private ListSettingEntry createNew(boolean isIntercepted) {
		if (customEdit != null && !isIntercepted)
			throw new IllegalStateException("Failed to intercept add!");

		E e = ctor.createNew();
		EntryConfig<E> config = new EntryConfig<>(this, e);
		rawList.add(c(config));
		if (!config.isInvalid())
			notifyChange();

		return new StyledListSettingEntry(this, config, rawList.size());
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>(this.rawList.size());
		insertElements(list);
		return list;
	}

	protected void insertElements(List<KeyValue<Setting>> list) {
		for (int i = 0; i < this.rawList.size(); i++) {
			EntryConfig<E> config = this.rawList.get(i);
			if (config.isInvalid()) {
				this.rawList.remove(i--);
				notifyChange();
				continue;
			}

			// Create setting entry
			ListSettingEntry entry = new StyledListSettingEntry(this, config, i);
			list.add(new KeyValue<>(entry.getId(), entry));
		}
	}

	@Override
	public void remove(ListSettingEntry entry) {
		super.remove(entry);
		notifyChange();
	}

	@EventListener
	private static void onInit(SettingActivityInitEvent event) {
		if (!(event.parent() instanceof ListSettingImpl<?> setting))
			return;

		// Hook add button
		if (setting.customEdit != null) {
			event.get("container", "mods-breadcrumb", "accent-button").setPressable(() -> {
				ListEntry<?> e = ((StyledListSettingEntry) setting.createNew(true)).entry;
				setting.customEdit.accept(c(e));
			});
		}
	}

	/**
	 * ListEntry -> Config adapter.
	 */
	protected static class EntryConfig<E extends ListEntry<E>> extends Config implements ListSettingConfig {

		private final ListSettingImpl<E> parent;
		public final E value;

		public EntryConfig(ListSettingImpl<E> parent, E value) {
			this.parent = parent;
			this.value = value;
		}

		@Override
		public @NotNull Component newEntryTitle() {
			return Component.text(value.createNew().getName());
		}

		@Override
		public List<Setting> toSettings(Setting parent) {
			List<Setting> settings = new ArrayList<>();
			for (BaseSetting<?> setting : value.toSettings()) {
				settings.add(c(setting));
				setting.create(parent);

				if (setting instanceof AbstractSetting<?, ?> as) {
					as.callback(this.parent::notifyChange);
				}
			}

			return settings;
		}

		@Override
		public @NotNull Component entryDisplayName() {
			return Component.text(value.getName());
		}

	}

	/**
	 * Proxied iterator that unwraps EntryConfig.
	 */
	private class Itr implements Iterator<E> {
		private final Iterator<EntryConfig<E>> source = ListSettingImpl.this.rawList.iterator();

		@Override
		public boolean hasNext() {
			return source.hasNext();
		}

		@Override
		public E next() {
			return source.next().value;
		}

		@Override
		public void remove() {
			source.remove();
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			source.forEachRemaining(e -> action.accept(e.value));
		}

	}

	/**
	 * List entries that support icons and descriptions.
	 */
	private static class StyledListSettingEntry extends ListSettingEntry {

		private final ListEntry<?> entry;
		private final boolean hasHookedEdit;

		public StyledListSettingEntry(ListSettingImpl<?> parent, EntryConfig<?> config, int index) {
			super(parent, config.entryDisplayName(), null, index);
			entry = config.value;

			hasHookedEdit = parent.customEdit != null;
			if (hasHookedEdit) {
				ButtonWidget widget = ButtonWidget.component(null, Textures.SpriteCommon.SETTINGS,
						() -> parent.customEdit.accept(c(entry)))
					.addId("mods-setting-advanced-button"); // Fix for button size

				// ModsSettingWidget resets the widget and its icon, which causes it to disappear.
				widget.icon().updateDefaultValue(widget.icon().get());

				setWidgets(new Widget[]{
					widget
				});
			} else
				addSettings(config);
		}

		@Override
		public Component getDescription() {
			String subtext = entry.subtextLaby4();
			return subtext == null ? null : Component.text(subtext);
		}

		@Override
		public boolean hasAdvancedButton() {
			return !hasHookedEdit;
		}

		@Override
		public Icon getIcon() {
			String resourceIcon = entry.resourceIcon();
			if (resourceIcon != null)
				return Icons.of(resourceIcon);

			ItemStack itemIcon = entry.itemIcon();
			if (itemIcon != null)
				return Icons.of(itemIcon);

			return super.getIcon();
		}
	}

	private static class SettingAccessor<E extends ListEntry<E>> extends ConfigPropertySettingAccessor {

		public final ArrayList<E> list = new ArrayList<>();

		public SettingAccessor() {
			super(null, null, null, null);
		}

		@Override
		public <T> T get() {
			return c(list);
		}

		@Override
		public Type getGenericType() {
			return new ParameterizedType() {
				public @NotNull Type @NotNull [] getActualTypeArguments() {
					return new Type[]{
						EntryConfig.class
					};
				}

				public @NotNull Type getRawType() {return EntryConfig.class;}

				public Type getOwnerType() {return null;}
			};
		}
	}
}
