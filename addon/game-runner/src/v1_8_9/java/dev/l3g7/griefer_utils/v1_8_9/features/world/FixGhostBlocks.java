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

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockBrokeEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK;

@Singleton
public class FixGhostBlocks extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Ghost-Blöcke entfernen")
		.description("Versucht, verbuggte / unsichtbare Blöcke zu entfernen.")
		.icon("crossed_out_block_outline");

	@EventListener
	private void onBlockBrokeEvent(BlockBrokeEvent event) {
		mc().getNetHandler().addToSendQueue(new C07PacketPlayerDigging(ABORT_DESTROY_BLOCK, event.pos, event.side));
	}

}
