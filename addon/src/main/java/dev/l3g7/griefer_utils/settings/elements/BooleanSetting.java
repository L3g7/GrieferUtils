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

package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;

import java.lang.reflect.Field;

import static dev.l3g7.griefer_utils.settings.elements.BooleanSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.settings.elements.BooleanSetting.TriggerMode.TOGGLE;

/**
 * A setting holding a boolean, represented in-game by a switch.
 */
public class BooleanSetting extends BooleanElement implements ElementBuilder<BooleanSetting>, ValueHolder<BooleanSetting, Boolean> {

	private static final Field keySettingField;
	private static final Field triggerModeSettingField;

	private final Storage<Boolean> storage = new ValueHolder.Storage<>(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	private final IconStorage iconStorage = new IconStorage();
	private KeySetting key;
	private final TriggerModeSetting triggerMode = new TriggerModeSetting();

	public BooleanSetting() {
		super("§cNo name set", null, v -> {}, false);
		custom("An", "Aus");
		setSettingEnabled(true);
		Reflection.set(this, (Consumer<Boolean>) this::set, "toggleListener");
	}

	@Override
	public BooleanSetting custom(String... args) {
		super.custom(args);
		return this;
	}

	@Override
	public Storage<Boolean> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

	public Field getSettingField(SettingsElement element) {
		if (element == key)
			return keySettingField;

		if (element == triggerMode)
			return triggerModeSettingField;

		return null;
	}

	public BooleanSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		if (getSubSettings().getElements().isEmpty())
			subSettings();

		subSettings.getElements().add(4, key = new KeySetting()
			.name("Taste")
			.description("Welche Taste " + whatActivates + " aktiviert.")
			.icon("key")
			.pressCallback(p -> {
				if (p || triggerMode.get() == HOLD)
					this.set(!this.get());
			}));

		if (defaultTriggerMode == null)
			return this;

		triggerMode.description("Halten: Aktiviert " + whatActivates + ", während die Taste gedrückt wird.\n" +
			"Umschalten: Schaltet " + whatActivates + " um, wenn die Taste gedrückt wird.");
		triggerMode.defaultValue(defaultTriggerMode);
		subSettings.getElements().add(5, triggerMode);
		subSettings.getElements().add(6, new HeaderSetting());
		return this;
	}

	static {
		try {
			keySettingField = BooleanSetting.class.getDeclaredField("key");
			triggerModeSettingField = BooleanSetting.class.getDeclaredField("triggerMode");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private class TriggerModeSetting extends DropDownSetting<TriggerMode> {

		private TriggerMode previousMode;

		public TriggerModeSetting() {
			super(TriggerMode.class);
			name("Auslösung");
			icon("lightning");
			defaultValue(TOGGLE);
			callback(m -> {
				if (previousMode != null && previousMode != m)
					BooleanSetting.this.set(false);

				previousMode = m;
			});
		}

	}

	public enum TriggerMode implements Named {

		HOLD("Halten"), TOGGLE("Umschalten");

		final String name;

		TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}


	}

}
