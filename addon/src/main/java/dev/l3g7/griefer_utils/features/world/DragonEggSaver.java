/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.BlockEvent.BlockClickEvent;
import dev.l3g7.griefer_utils.event.events.BlockEvent.BlockInteractEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class DragonEggSaver extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Drachenei-Saver")
		.description("Verhindert Klicks auf Dracheneier.")
		.icon(Material.DRAGON_EGG);

	@EventListener
	private void onBlockInteract(BlockInteractEvent event) {
		if (world().getBlockState(event.pos).getBlock() == Blocks.dragon_egg)
			event.cancel();
	}

	@EventListener
	private void onBlockClick(BlockClickEvent event) {
		if (world().getBlockState(event.pos).getBlock() == Blocks.dragon_egg)
			event.cancel();
	}

}
