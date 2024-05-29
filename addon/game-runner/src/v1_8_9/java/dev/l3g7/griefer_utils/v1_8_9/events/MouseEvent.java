/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MouseEvent extends Event {

	public final int x;
	public final int y;
	public final int dx;
	public final int dy;
	public final int dwheel;
	public final int button;
	public final boolean buttonstate;
	public final long nanoseconds;

	public MouseEvent() {
		this.x = Mouse.getEventX();
		this.y = Mouse.getEventY();
		this.dx = Mouse.getEventDX();
		this.dy = Mouse.getEventDY();
		this.dwheel = Mouse.getEventDWheel();
		this.button = Mouse.getEventButton();
		this.buttonstate = Mouse.getEventButtonState();
		this.nanoseconds = Mouse.getEventNanoseconds();
	}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

	    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", shift = At.Shift.BEFORE, remap = false), cancellable = true)
	    public void injectRunTick(CallbackInfo ci) {
		    if (new MouseEvent().fire().isCanceled())
				ci.cancel();
	    }

	}

}