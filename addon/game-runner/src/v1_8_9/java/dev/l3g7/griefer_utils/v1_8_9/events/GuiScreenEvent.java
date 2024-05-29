/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.api.Laby;
import net.labymod.v1_8_9.client.gui.screen.LabyScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public abstract class GuiScreenEvent extends Event {

	public final GuiScreen gui;

	public GuiScreenEvent(Object gui) {
		if (LABY_4.isActive() && gui instanceof LabyScreenRenderer) {
			try { // FIXME: Does this cause problems?
				Object mostInnerScreen = Laby.labyAPI().minecraft().minecraftWindow().mostInnerScreen();
				if (mostInnerScreen instanceof GuiScreen)
					gui = mostInnerScreen;
			} catch (Throwable ignored) {}
		}

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
				System.out.println(new StringBuilder().append("From ").append(screen).append(", canceled: ").append(event.isCanceled()).append(", gui: ").append(event.gui).toString());
				return event.isCanceled() ? CANCEL_INDICATOR : event.gui;
		    }

			@Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
			public void injectDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
				System.out.println(new StringBuilder().append("injectDisplayGuiScreen HEAD: ").append(guiScreenIn).append(" < ").append(mc().currentScreen).toString());
				if (guiScreenIn == CANCEL_INDICATOR)
					ci.cancel();
				else
					cancelOpenPacket = false;
			}

			@Inject(method = "displayGuiScreen", at = @At("TAIL"), cancellable = true)
			public void injectDisplayGuiScreen2(GuiScreen guiScreenIn, CallbackInfo ci) {
				System.out.println(new StringBuilder().append("injectDisplayGuiScreen TAIL: ").append(guiScreenIn).append(" < ").append(mc().currentScreen).toString());
			}

		}

		@Mixin(S2DPacketOpenWindow.class)
		private static class MixinS2DPacketOpenWindow {

			@Shadow
			private int windowId;

			@Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At("HEAD"))
		    private void injectProcessPacketHead(INetHandlerPlayClient handler, CallbackInfo ci) {
				cancelOpenPacket = true;
		    }

			@Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At("RETURN"))
			private void injectProcessPacketTail(INetHandlerPlayClient handler, CallbackInfo ci) {
				if (!cancelOpenPacket)
					return;

				MinecraftUtil.player().openContainer.windowId = 0;
				mc().getNetHandler().addToSendQueue(new C0DPacketCloseWindow(windowId));
			}

		}

	}
}
