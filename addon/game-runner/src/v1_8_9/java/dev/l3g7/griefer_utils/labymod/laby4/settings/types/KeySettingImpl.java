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
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.MouseInputEvent;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

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
		EventRegisterer.registerWeak(this);
	}

	@Override
	protected Widget[] createWidgets() {
		MultiKeybindWidget widget = new MultiKeybindWidget(v -> this.set(Arrays.stream(v).map(k -> k instanceof MouseButton ? -k.getId() : k.getId()).collect(Collectors.toSet())));
		widget.placeholder(placeholder);
		widget.setKeys(get().stream().map(KeySettingImpl::getKey).collect(Collectors.toSet()));
		callback(newValue -> {
			Set<Key> keys = widget.getKeys();
			keys.clear();
			get().stream().map(KeySettingImpl::getKey).forEach(keys::add);
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
		if (Mouse.getEventButtonState() && triggersInContainers)
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

		pressed = get().stream().allMatch(i -> getKey(i).isPressed());
		pressCallbacks.forEach(c -> c.accept(pressed));
	}

	private static Key getKey(int i) {
		return i < 0 ? MouseButton.get(-i) : Key.get(i);
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = MultiKeybindWidget.class,  remap = false)
	private static abstract class MixinMultiKeybindWidget {

		@Shadow
		abstract boolean keyPressed(Key key, InputType type);

		@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
		private void injectMouseClicked(MutableMouse mouse, MouseButton mouseButton, CallbackInfoReturnable<Boolean> cir) {
			if (!mouseButton.getActualName().startsWith("M"))
				return;

			this.keyPressed(mouseButton, InputType.ACTION);
			cir.setReturnValue(true);
		}

	}

}
