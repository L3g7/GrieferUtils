/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.GOTO;

public class InputEvent extends Event {

	public static class MouseInputEvent extends InputEvent {}
	public static class KeyInputEvent extends InputEvent {}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Inject(method = "runTick", at = @At(value = "JUMP", opcode = GOTO, ordinal = 1, shift = At.Shift.BEFORE), slice = @Slice(
			from = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/Minecraft;updateDebugProfilerName(I)V")
		))
		public void injectKeyboardEvent(CallbackInfo ci) {
			new KeyInputEvent().fire();
		}

		@Inject(method = "runTick", at = @At(value = "JUMP", opcode = GOTO, ordinal = 0, shift = At.Shift.BEFORE), slice = @Slice(
			from = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V")
		))
		public void injectMouseEvent(CallbackInfo ci) {
			new MouseInputEvent().fire();
		}

	}

}
