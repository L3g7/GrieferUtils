/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.labymod.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class NumberSettingImpl extends AbstractSettingImpl<NumberSetting, Integer> implements NumberSetting {

	private Component placeholder = null;
	private int min, max = Integer.MAX_VALUE;

	public NumberSettingImpl() {
		super(JsonPrimitive::new, JsonElement::getAsInt, 0);
	}

	@Override
	protected Widget[] createWidgets() {
		TextFieldWidget widget = new TextFieldWidget();
		NumberSettingWidget nsWidget = c(widget);
		nsWidget.grieferUtils$setIsNumberSettingWidget();
		widget.placeholder(placeholder);

		widget.validator(v -> {
			try {
				Integer.parseInt(v);
				return true;
			} catch (NumberFormatException e) {
				return v.isBlank() || v.trim().equals("-");
			}
		});
		widget.submitHandler(text -> {
			set(MathHelper.clamp((text.isBlank() || text.trim().equals("-")) ? 0 : Integer.parseInt(text), min, max));
			widget.setCursorAtEnd();
			nsWidget.grieferUtils$setViewIndexZero();
		});

		widget.setText(String.valueOf(get()));
		widget.maximalLength(11);
		callback(v -> widget.setText(String.valueOf(v), true));

		return new Widget[]{widget};
	}

	@Override
	public NumberSetting placeholder(String placeholder) {
		this.placeholder = Component.text(placeholder);
		return this;
	}

	@Override
	public NumberSetting min(int min) {
		this.min = min;
		return this;
	}

	@Override
	public NumberSetting max(int max) {
		this.max = max;
		return this;
	}

	public interface NumberSettingWidget {

		void grieferUtils$setIsNumberSettingWidget();

		void grieferUtils$setViewIndexZero();

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = TextFieldWidget.class, remap = false)
	public static abstract class MixinChatMessagesWidget implements NumberSettingWidget {

		@Shadow
		protected int viewIndex;

		@Shadow
		protected abstract void submit();

		@Unique
		private boolean grieferUtils$isNumberSettingWidget = false;

		@Override
		public void grieferUtils$setIsNumberSettingWidget() {
			grieferUtils$isNumberSettingWidget = true;
		}

		@Override
		public void grieferUtils$setViewIndexZero() {
			viewIndex = 0;
		}

		@Inject(method = "setFocused", at = @At("HEAD"))
		private void injectSetFocused(boolean focused, CallbackInfo ci) {
			if (!grieferUtils$isNumberSettingWidget)
				return;

			// Make leaving focus trigger the submit handler
			TextFieldWidget self = c(this);
			if (self.isFocused() && !focused) {
				self.setCursorAtStart();
				submit();
			}
		}

		@Inject(method = "insertText", at = @At("HEAD"), cancellable = true)
		private void injectInsertText(String text, CallbackInfo ci) {
			if (!grieferUtils$isNumberSettingWidget)
				return;

			// Handle minus sign
			TextFieldWidget self = c(this);
			if (text.equals("-")) {
				if (self.getText().contains("-")) {
					ci.cancel();
					return;
				}

				if (self.getCursorIndex() != 0) {
					ci.cancel();
					self.setText("-");
					self.setCursorAtEnd();
					viewIndex = 0;
					return;
				}
			}

			if (self.getText().equals("0") && self.isCursorAtEnd()) {
				ci.cancel();
				self.setText(text);
				self.setCursorAtEnd();
				viewIndex = 0;
			}
		}

	}

}
