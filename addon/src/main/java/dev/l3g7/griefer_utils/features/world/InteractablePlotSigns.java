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
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class InteractablePlotSigns extends Feature {

	private static final Pattern PLOT_ID_PATTERN = Pattern.compile("ID: (-?\\d+);(-?\\d+)");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Interagierbare Plot-Schilder")
		.description("Führt bei Klick auf ein Plot-Schild /p i aus.")
		.icon("wooden_board");

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		TileEntity te = world().getTileEntity(event.packet.getPosition());

		if (!(te instanceof TileEntitySign))
			return;

		TileEntitySign tes = (TileEntitySign) te;
		if (tes.signText[0] == null)
			return;

		String txt = tes.signText[0].getUnformattedText();
		Matcher matcher = PLOT_ID_PATTERN.matcher(txt);
		if (!matcher.matches())
			return;

		send("/p i " + matcher.group(1) + ";" + matcher.group(2));
		event.cancel();
	}

}
