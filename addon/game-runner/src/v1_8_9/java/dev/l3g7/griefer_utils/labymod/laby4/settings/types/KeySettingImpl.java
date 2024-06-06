/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.MouseInputEvent;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KeySettingImpl extends AbstractSettingImpl<KeySetting, Set<Integer>> implements KeySetting {

	private Component placeholder = null;
	private final List<Consumer<Boolean>> pressCallbacks = new ArrayList<>();
	private boolean pressed;
	private boolean triggersInContainers = false;

	public KeySettingImpl() {
		super(
			values -> {
				JsonArray array = new JsonArray();
				values.stream().map(JsonPrimitive::new).forEach(array::add);
				return array;
			},
			elem -> new LinkedHashSet<>(
				StreamSupport.stream(elem.getAsJsonArray().spliterator(), false)
					.map(JsonElement::getAsInt)
					.collect(Collectors.toList())),
			new TreeSet<>()
		);
		EventRegisterer.register(this);
	}

	@Override
	protected Widget[] createWidgets() {
		MultiKeybindWidget widget = new MultiKeybindWidget(v -> this.set(Arrays.stream(v).map(Key::getId).collect(Collectors.toSet())));
		widget.placeholder(placeholder);
		widget.setKeys(get().stream().map(Key::get).collect(Collectors.toSet()));
		callback(newValue -> {
			Set<Key> keys = widget.getKeys();
			keys.clear();
			get().stream().map(Key::get).forEach(keys::add);
		});

		return new Widget[]{widget};
	}

	@Override
	public KeySetting placeholder(String placeholder) {
		this.placeholder = Component.text(placeholder);
		return this;
	}

	@Override
	public KeySetting pressCallback(Consumer<Boolean> callback) {
		pressCallbacks.add(callback);
		return this;
	}

	public KeySetting triggersInContainers() {
		triggersInContainers = true;
		return this;
	}


	@EventListener
	public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!Keyboard.isRepeatEvent() && triggersInContainers)
			onPress(Keyboard.getEventKey());
	}

	@EventListener
	public void onGuiMousePress(GuiScreenEvent.MouseInputEvent.Post event) {
		if (triggersInContainers)
			onPress(-Mouse.getEventButton());
	}

	@EventListener
	public void onKeyPress(KeyInputEvent event) {
		if (!Keyboard.isRepeatEvent())
			onPress(Keyboard.getEventKey());
	}

	@EventListener
	private void onMousePress(MouseInputEvent event) {
		if (Mouse.getEventButton() != -1)
			onPress(-Mouse.getEventButton());
	}

	private void onPress(int code) {
		if (!get().contains(code))
			return;

		pressed = get().stream().allMatch(i -> Key.get(i).isPressed());
		pressCallbacks.forEach(c -> c.accept(pressed));
	}

}
