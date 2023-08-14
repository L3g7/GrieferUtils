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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Cancelable
public class GuiOpenEvent extends Event {

	public GuiScreen gui;

	public GuiOpenEvent(GuiScreen gui) {
		this.gui = gui;
	}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Shadow
		public WorldClient theWorld;

		@Shadow
		public EntityPlayerSP thePlayer;

		@Inject(method = "displayGuiScreen", at = @At("HEAD"))
	    public void injectDisplayGuiScreen(GuiScreen screen, CallbackInfo ci) {
			if (screen == null) {
				if (theWorld == null)
					screen = new GuiMainMenu();
				else if (thePlayer.getHealth() <= 0)
					screen = new GuiGameOver();
			}

			GuiOpenEvent event = new GuiOpenEvent(screen);
			MinecraftForge.EVENT_BUS.post(event);
			screen = event.gui;
	    }

	}

}