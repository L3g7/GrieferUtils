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

package dev.l3g7.griefer_utils.features.world.furniture;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.furniture.download.AssetsCreator;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;

@Singleton
public class Furniture extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Möbel"
			+ "\n§eInstabil \u26A0")
		.description("Fügt die MysteryMod-Möbel hinzu."
			+ "\n\n§eKann nicht startendem Minecraft führen!")
		.icon("chair")
		.callback(active -> {
			if (!active) {
				if (FurnitureLoader.loaded)
					displayAchievement(null, "§e§lStarte Minecraft neu", "§eUm die Möbel zu deaktivieren, muss Minecraft neu gestartet werden.");
				return;
			}

			if (!FurnitureLoader.loaded) {
				displayAchievement(null, "§f§lMöbel werden geladen...", "§fBitte habe einen Moment Geduld.");
				AssetsCreator.createAssets();
			}
		});

	@EventListener
	public void onPacket(PacketEvent.PacketReceiveEvent event) {
		if (!(event.packet instanceof S3FPacketCustomPayload))
			return;

		S3FPacketCustomPayload cpp = (S3FPacketCustomPayload) event.packet;

		if (!cpp.getChannelName().equals("MM|CustomBlocks") && !cpp.getChannelName().equals("cb:init")
			|| !FurnitureLoader.loaded
			|| !"StartHandshake".equals(cpp.getBufferData().readStringFromBuffer(Short.MAX_VALUE)))
			return;

		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeString("{\"protocol\":4}");
		C17PacketCustomPayload packet = new C17PacketCustomPayload("MM|CustomBlocks", buf);
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
	}

}
