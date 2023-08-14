/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.event.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public abstract class GuiScreenEvent extends Event {

	public final GuiScreen gui;

	public GuiScreenEvent(Object gui) {
		this.gui = (GuiScreen) gui;
	}

	public static class InitGuiEvent extends GuiScreenEvent {

		public InitGuiEvent(Object gui) {
			super(gui);
		}

	}

	public static class DrawScreenEvent extends GuiScreenEvent {

		public DrawScreenEvent(Object gui) {
			super(gui);
		}

		@Mixin(EntityRenderer.class)
		private static class MixinEntityRenderer {

			@Shadow
			private Minecraft mc;

			@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", shift = At.Shift.AFTER))
			public void injectUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
				EVENT_BUS.post(new DrawScreenEvent(mc.currentScreen));
			}

		}

	}

	public abstract static class MouseInputEvent extends GuiScreenEvent {

		public MouseInputEvent(Object gui) {
			super(gui);
		}

		@Cancelable
		public static class Pre extends MouseInputEvent {
			public Pre(Object gui) {
				super(gui);
			}
		}

		public static class Post extends MouseInputEvent {
			public Post(Object gui) {
				super(gui);
			}
		}
	}

	public abstract static class KeyboardInputEvent extends GuiScreenEvent {

		public KeyboardInputEvent(Object gui) {
			super(gui);
		}

		@Cancelable
		public static class Pre extends KeyboardInputEvent {
			public Pre(Object gui) {
				super(gui);
			}
		}

		public static class Post extends KeyboardInputEvent {
			public Post(Object gui) {
				super(gui);
			}
		}
	}

	@Mixin(GuiScreen.class)
	private static class MixinGuiScreen {

		@Inject(method = "setWorldAndResolution", at = @At("TAIL"))
		public void injectSetWorldAndResolution(Minecraft mc, int width, int height, CallbackInfo ci) {
			EVENT_BUS.post(new InitGuiEvent(this));
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V", shift = At.Shift.BEFORE))
		public void injectMouseInputPre(CallbackInfo ci) {
			EVENT_BUS.post(new MouseInputEvent.Pre(this));
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V", shift = At.Shift.AFTER))
		public void injectMouseInputPost(CallbackInfo ci) {
			EVENT_BUS.post(new MouseInputEvent.Pre(this));
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V", shift = At.Shift.BEFORE))
		public void injectKeyboardInputPre(CallbackInfo ci) {
			EVENT_BUS.post(new KeyboardInputEvent.Pre(this));
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V", shift = At.Shift.AFTER))
		public void injectKeyboardInputPost(CallbackInfo ci) {
			EVENT_BUS.post(new KeyboardInputEvent.Pre(this));
		}

	}

}
