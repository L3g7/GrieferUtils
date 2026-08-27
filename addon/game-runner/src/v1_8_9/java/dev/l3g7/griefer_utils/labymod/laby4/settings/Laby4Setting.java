/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextDecoration;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.action.Switchable;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.SettingHandler;
import net.labymod.api.configuration.settings.accessor.SettingAccessor;
import net.labymod.api.configuration.settings.type.AbstractSettingRegistry;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.event.labymod.config.SettingCreateEvent;
import net.labymod.api.revision.Revision;
import net.labymod.api.revision.SimpleRevision;
import net.labymod.api.util.KeyValue;
import net.labymod.api.util.version.SemanticVersion;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsSettingWidget;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsTileWidget;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@SuppressWarnings("unchecked")
public interface Laby4Setting<S extends AbstractSetting<S, V>, V> extends AbstractSetting<S, V>, Setting {

	// Helper methods

	default void init() {
		SettingElement self = c(this);
		self.setAccessor(new Accessor(this));
		self.setHandler(new SettingHandler() {
			public void created(Setting setting) {}

			public void initialized(Setting setting) {}

			public boolean isEnabled(Setting setting) {return getStorage().enabled;}
		});
	}

	// BaseSetting

	@Override
	default String name() {
		return getStorage().name;
	}

	@Override
	default S name(String name) {
		getStorage().name = name.trim();
		return (S) this;
	}

	@Override
	default S description(String... description) {
		if (description.length == 0)
			getStorage().description = null;
		else
			getStorage().description = String.join("\n", description).trim();
		return (S) this;
	}

	@Override
	default S icon(String icon) {
		return icon(Icons.of(icon));
	}

	@Override
	default S icon(ItemStack icon) {
		return icon(Icons.of(icon));
	}

	default S icon(Icon icon) {
		getStorage().icon = icon;
		return (S) this;
	}

	@Override
	default S subSettings(BaseSetting<?>... settings) {
		getElements().clear();
		return subSettings(Arrays.asList(settings));
	}

	@Override
	default S subSettings(List<BaseSetting<?>> settings) {
		AbstractSettingRegistry self = c(this);

		settings = new ArrayList<>(settings);
		settings.removeIf(Objects::isNull);
		settings.forEach(s -> s.setParent(this));
		self.addSettings((List<Setting>) c(settings));
		return (S) this;
	}

	@Override
	default S addSetting(BaseSetting<?> setting) {
		AbstractSettingRegistry self = c(this);
		setting.setParent(this);
		self.addSetting(c(setting));
		return (S) this;
	}

	@Override
	default S addSetting(int index, BaseSetting<?> setting) {
		setting.setParent(this);

		net.labymod.api.configuration.settings.type.AbstractSetting lmSetting = c(setting);

		lmSetting.setParent(this);
		getElements().add(index, new KeyValue<>(lmSetting.getId(), lmSetting));
		if (isInitialized() && lmSetting instanceof AbstractSettingRegistry)
			lmSetting.initialize();

		return (S) this;
	}

	@Override
	default List<BaseSetting<?>> getChildSettings() {
		return c(getElements().stream()
			.map(KeyValue::getValue)
			.filter(BaseSetting.class::isInstance)
			.collect(Collectors.toList()));
	}

	@Override
	default void create(Object parent) {
		AbstractSettingRegistry self = c(this);
		self.setParent(c(parent));

		bubbleSince(parent);
		if (since() != null)
			((SettingElement) this).setRevision(new GrieferUtilsRevision(since()));

		Laby.fireEvent(new SettingCreateEvent(self));
	}

	@Override
	default void bubbleSince(Object parent) {
		// NO-OP
	}

	// AbstractSetting

	@Override
	ExtendedStorage<V> getStorage();

	@Override
	default S enabled(boolean enabled) {
		getStorage().enabled = enabled;
		return (S) this;
	}

	@Override
	default S extend() {
		SettingElement self = c(this);
		self.setExtended(true);
		return (S) this;
	}

	@Override
	default boolean isOpen() {
		ModsActivity modsActivity = Laby4Util.getModsActivity();
		if (modsActivity == null)
			return false;

		Deque<SettingElement> openSettings = Reflection.get(modsActivity, "openSettings");

		Setting current = openSettings.peekFirst();
		while (current != null) {
			if (current == this)
				return true;

			current = current.parent();
		}

		return false;
	}

	class ExtendedStorage<V> extends Storage<V> {

		public String name = "§cNo name set";
		public String description = null;
		public Icon icon;
		public boolean enabled = true;

		public ExtendedStorage(Function<V, JsonElement> encodeFunc, Function<JsonElement, V> decodeFunc, V fallbackValue) {
			super(encodeFunc, decodeFunc, fallbackValue);
		}

	}

	class Accessor implements SettingAccessor {

		private final Laby4Setting<?, ?> impl;
		private final ConfigProperty<Object> property;

		public Accessor(Laby4Setting<?, ?> impl) {
			this.impl = impl;
			property = new ConfigProperty<>(get());
		}

		public Class<?> getType() {
			return null;
		}

		public Type getGenericType() {
			return null;
		}

		public Field getField() {
			return null;
		}

		public Config config() {
			return null;
		}

		public <T> void set(T value) {
			impl.set(c(value));
		}

		public <T> T get() {
			return (T) impl.get();
		}

		public ConfigProperty<?> property() {
			property.set(get());
			return property;
		}

		public SettingElement setting() {
			return c(impl);
		}

	}

	class GrieferUtilsRevision extends SimpleRevision {

		public final UpdateInfo info;

		public GrieferUtilsRevision(UpdateInfo info) {
			super("griefer_utils", new SemanticVersion(0, 0, 0), "0001-01-01");
			this.info = info;
		}

		@Override
		public String getDisplayName() {
			return info.toString();
		}

		@Override
		public boolean isRelevant() {
			return info.isVisible();
		}

		public IconWidget createBadge(AbstractSetting<?, ?> source) {
			IconWidget newBadge = new IconWidget(Textures.SpriteCommon.NEW);
			newBadge.addId("new-badge");
			newBadge.setHoverComponent(Component.text(getDisplayName()));

			source.callback(() -> {
				if (!info.bubbled()) {
					info.hide();
					newBadge.setVisible(false);
				}
			});
			return newBadge;
		}
	}

	/**
	 * Copy of ModsOptionsBuilder#newBadge, but accessible
	 */
	static IconWidget newBadge(Revision revision) {
		IconWidget newBadge = new IconWidget(Textures.SpriteCommon.NEW);
		newBadge.addId("new-badge");
		newBadge.setHoverComponent(Component
			.translatable("labymod.misc.introduced")
			.color(NamedTextColor.BLUE)
			.argument(Component
				.text(revision.getDisplayName())
				.color(NamedTextColor.WHITE)
				.decorate(TextDecoration.BOLD)));

		return newBadge;
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ModsSettingWidget.class, remap = false)
	class MixinSettingWidget {
		@Shadow
		@Final
		private SettingElement element;

		@Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/labymod/child/mods/ModsOptionsBuilder;newBadge(Lnet/labymod/api/revision/Revision;)Lnet/labymod/api/client/gui/screen/widget/widgets/renderer/IconWidget;"))
		public IconWidget redirectNewBadge(Revision revision) {
			if (!(revision instanceof GrieferUtilsRevision rev))
				return newBadge(revision);

			return rev.createBadge((AbstractSetting<?, ?>) element);
		}
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ModsTileWidget.class, remap = false)
	class MixinModsTileWidget {

		@Shadow
		private Widget actionWidget;

		@Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/gui/screen/activity/activities/labymod/child/mods/ModsOptionsBuilder;newBadge(Lnet/labymod/api/revision/Revision;)Lnet/labymod/api/client/gui/screen/widget/widgets/renderer/IconWidget;"))
		public IconWidget redirectNewBadge(Revision revision) {
			if (!(revision instanceof GrieferUtilsRevision rev))
				return newBadge(revision);

			Switchable switchable = Reflection.get(actionWidget, "switchable");
			Laby4Widget widget = Reflection.get(switchable, "arg$2");
			return rev.createBadge(widget.getSetting());
		}
	}

}
