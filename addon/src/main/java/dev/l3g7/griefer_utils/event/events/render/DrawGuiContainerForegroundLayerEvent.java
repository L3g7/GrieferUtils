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

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.core.injection.mixin.MixinGuiChest;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DrawGuiContainerForegroundLayerEvent extends Event {

	public final GuiChest chest;

	private DrawGuiContainerForegroundLayerEvent(GuiChest chest) {
		this.chest = chest;
	}

	/**
	 * Triggered by {@link MixinGuiChest}
	 */
	public static void post(GuiChest chest) {
		MinecraftForge.EVENT_BUS.post(new DrawGuiContainerForegroundLayerEvent(chest));
	}

}