/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;

public class StringSettingImpl extends AbstractSettingImpl<StringSetting, String> implements StringSetting {

	private int maxLength = Integer.MAX_VALUE;
	private Component placeholder = null;
	private Predicate<String> validator = v -> true;

	public StringSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsString, "");
	}

	@Override
	protected Widget[] createWidgets() {
		TextFieldWidget widget = new TextFieldWidget();

		widget.placeholder(placeholder);
		widget.maximalLength(maxLength);
		widget.validator(validator);

		widget.setText(get());
		widget.updateListener(this::set);
		callback(v -> widget.setText(v, true));

		return new Widget[]{widget};
	}

	@Override
	public StringSetting maxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	@Override
	public StringSetting placeholder(String placeholder) {
		this.placeholder = Component.text(placeholder);
		return this;
	}

	@Override
	public StringSetting validator(Predicate<String> validator) {
		this.validator = validator;
		return this;
	}

}
