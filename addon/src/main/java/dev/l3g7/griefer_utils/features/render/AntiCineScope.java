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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

@Singleton
public class AntiCineScope extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Schwarze Balken unterdrücken")
		.description("Unterdrückt das Erzwingen der schwarzen Balken auf der oberen und unteren Seite des Bildschirms durch GrieferGames.")
		.icon("crossed_out_camera");

	@EventListener
	private void onPacketReceive(PacketReceiveEvent<S3FPacketCustomPayload> event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		if (!event.packet.getChannelName().equals("labymod3:main") && !event.packet.getChannelName().equals("LMC"))
			return;

		if (event.packet.getBufferData().readableBytes() <= 0)
			return;

		if ("cinescopes".equals(event.packet.getBufferData().readStringFromBuffer(Short.MAX_VALUE)))
			event.cancel();
	}

}
