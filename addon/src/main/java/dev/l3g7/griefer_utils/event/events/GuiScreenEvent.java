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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class GuiScreenEvent extends Event {

	public final GuiScreen gui;

	public GuiScreenEvent(Object gui) {
		this.gui = (GuiScreen) gui;
	}

	public static class GuiInitEvent extends GuiScreenEvent {

		public GuiInitEvent(Object gui) {
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
				new DrawScreenEvent(mc.currentScreen).fire();
			}

		}

	}

	public abstract static class MouseInputEvent extends GuiScreenEvent {

		public MouseInputEvent(Object gui) {
			super(gui);
		}

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
			new GuiInitEvent(this).fire();
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V", shift = At.Shift.BEFORE), cancellable = true)
		public void injectMouseInputPre(CallbackInfo ci) {
			if (new MouseInputEvent.Pre(this).fire().isCanceled())
				ci.cancel();
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V", shift = At.Shift.AFTER))
		public void injectMouseInputPost(CallbackInfo ci) {
			new MouseInputEvent.Post(this).fire();
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V", shift = At.Shift.BEFORE), cancellable = true)
		public void injectKeyboardInputPre(CallbackInfo ci) {
			if (new KeyboardInputEvent.Pre(this).fire().isCanceled())
				ci.cancel();
		}

		@Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V", shift = At.Shift.AFTER))
		public void injectKeyboardInputPost(CallbackInfo ci) {
			new KeyboardInputEvent.Post(this).fire();
		}

	}

	public static class GuiOpenEvent<G extends GuiScreen> extends TypedEvent<GuiOpenEvent<G>> {

		public static final GuiScreen CANCEL_INDICATOR = new GuiWinGame();
		public static boolean cancelOpenPacket = false;

		public G gui;

		public GuiOpenEvent(G gui) {
			this.gui = gui;
		}

		@Mixin(Minecraft.class)
		private static abstract class MixinMinecraft {

			@Shadow
			public WorldClient theWorld;

			@Shadow
			public EntityPlayerSP thePlayer;

			@ModifyVariable(method = "displayGuiScreen", at = @At("HEAD"), argsOnly = true)
		    public GuiScreen injectDisplayGuiScreen(GuiScreen screen) {
				if (screen == null) {
					if (theWorld == null)
						screen = new GuiMainMenu();
					else if (thePlayer.getHealth() <= 0)
						screen = new GuiGameOver();
				}

				GuiOpenEvent<?> event = new GuiOpenEvent<>(screen).fire();
				return event.isCanceled() ? CANCEL_INDICATOR : event.gui;
		    }

			@Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
			public void injectDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
				if (guiScreenIn == CANCEL_INDICATOR)
					ci.cancel();
				else
					cancelOpenPacket = false;
			}

		}

		@Mixin(S2DPacketOpenWindow.class)
		private static class MixinS2DPacketOpenWindow {

		    @Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At("HEAD"))
		    private void injectProcessPacketHead(INetHandlerPlayClient handler, CallbackInfo ci) {
				cancelOpenPacket = true;
		    }

			@Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At("RETURN"))
			private void injectProcessPacketTail(INetHandlerPlayClient handler, CallbackInfo ci) {
				if (cancelOpenPacket)
					MinecraftUtil.player().openContainer.windowId = 0;
			}

		}

	}
}
