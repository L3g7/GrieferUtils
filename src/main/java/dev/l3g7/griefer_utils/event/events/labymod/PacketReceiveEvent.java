/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.event.events.labymod;

import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import net.labymod.api.EventManager;
import net.labymod.main.LabyMod;
import net.labymod.utils.Consumer;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

/**
 * A forge event for LabyMod's {@link EventManager#registerOnIncomingPacket(Consumer)}.
 */
public class PacketReceiveEvent extends Event {

	public final Packet<?> packet;

	private PacketReceiveEvent(Packet<?> packet) {
		this.packet = packet;
	}

	@OnEnable
	private static void register() {
		LabyMod.getInstance().getEventManager().registerOnIncomingPacket(p -> EVENT_BUS.post(new PacketReceiveEvent((Packet<?>) p)));
	}

}
