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

import dev.l3g7.griefer_utils.core.event_bus.Event.TypedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiOpenEvent<G extends GuiScreen> extends TypedEvent<GuiOpenEvent<G>> {

	public static final GuiScreen CANCEL_INDICATOR = new GuiWinGame();

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
		}

	}

}