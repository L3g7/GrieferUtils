/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.misc.functions.Function;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.SettingHandler;
import net.labymod.api.configuration.settings.accessor.SettingAccessor;
import net.labymod.api.configuration.settings.type.AbstractSettingRegistry;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.event.labymod.config.SettingCreateEvent;
import net.labymod.api.util.KeyValue;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

@SuppressWarnings("unchecked")
public interface BaseSettingImpl<S extends AbstractSetting<S, V>, V> extends AbstractSetting<S, V>, Setting {

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

	default void create(Config config, Setting parent) {
		getStorage().config = config;

		AbstractSettingRegistry self = c(this);
		self.setParent(parent);

		Laby.fireEvent(new SettingCreateEvent(self));
	}

	/**
	 * @return A SettingWidget where the widgets aren't in the input-wrapper div but its parent.
	 */
	default SettingWidget createUnwrappedWidget(Widget... widgets) {
		SettingWidget w = new SettingWidget(this, false);

		if (widgets == null || widgets.length == 0)
			return w;

		// Remove widgets from div
		if (this instanceof SettingElement s)
			s.setWidgets(null);


		// Add widgets to parent
		SettingsImpl.hookChildAdd(w, e -> {
			if (e.childWidget() instanceof FlexibleContentWidget parent)
				for (Widget widget : widgets)
					parent.addContent(widget);
		});

		return w;
	}

	// BaseSetting

	@Override
	default String name() {
		return getStorage().name;
	}

	@Override
	default S name(String name) {
		getStorage().name = name.trim();
		SettingElement self = c(this);
		self.setSearchTags(new String[]{name.trim()});
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
	default S icon(Object icon) {
		getStorage().icon = SettingsImpl.buildIcon(icon);
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
		self.addSettings((List<Setting>) c(settings));
		return (S) this;
	}

	@Override
	default List<BaseSetting<?>> getSubSettings() {
		return c(getElements().stream().map(KeyValue::getValue).collect(Collectors.toList()));
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


	class ExtendedStorage<V> extends Storage<V> {

		public String name = "§cNo name set";
		public String description = null;
		public Icon icon;
		public boolean enabled = true;

		public Config config;

		public ExtendedStorage(Function<V, JsonElement> encodeFunc, Function<JsonElement, V> decodeFunc, V fallbackValue) {
			super(encodeFunc, decodeFunc, fallbackValue);
		}

	}

	class Accessor implements SettingAccessor {

		private final BaseSettingImpl<?, ?> impl;
		private final ConfigProperty<Object> property;

		public Accessor(BaseSettingImpl<?, ?> impl) {
			this.impl = impl;
			property = new ConfigProperty<>(get());
		}

		public Class<?> getType() {
			return null;
		}

		public @Nullable Type getGenericType() {
			return null;
		}

		public Field getField() {
			return null;
		}

		public Config config() {
			return impl.getStorage().config;
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

}
