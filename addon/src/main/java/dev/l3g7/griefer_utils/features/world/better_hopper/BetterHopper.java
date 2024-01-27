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

package dev.l3g7.griefer_utils.features.world.better_hopper;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.BlockEvent.BlockInteractEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.utils.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING;
import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING;

@Singleton
public class BetterHopper extends Feature {

	static final BooleanSetting fillBoxes = new BooleanSetting()
		.name("Anzeigeboxen füllen")
		.description("Ob die Boxen der Anzeige gefüllt werden sollen.")
		.defaultValue(true)
		.icon(ItemUtil.createItem(Blocks.wool, 14, false));

	static final NumberSetting displayTime = new NumberSetting()
		.name("Anzeigedauer")
		.description("Wie lange die optische Anzeige aktiv bleiben soll, in Sekunden.")
		.icon("hourglass")
		.defaultValue(10);

	static final BooleanSetting betterVisualisation = new BooleanSetting()
		.name("Bessere optische Trichter-Anzeige")
		.description("Ersetzt die Partikel der optischen Trichter Anzeige durch Boxen / Linien.")
		.icon(Material.EYE_OF_ENDER)
		.subSettings(displayTime, fillBoxes);

	static final BooleanSetting showRange = new BooleanSetting()
		.name("Trichterreichweite anzeigen")
		.description("Zeigt die Trichterreichweite an.")
		.icon("ruler");

	static final BooleanSetting showSourceHopper = new BooleanSetting()
		.name("Ausgangstrichter anzeigen")
		.description("Zeigt beim Verbinden eines Trichters den Trichter an, von dem aus verbunden wird.")
		.icon(Material.HOPPER);

	private static final NumberSetting lastHoppersLimit = new NumberSetting()
		.name("Maximale Anzahl an Trichter")
		.description("Wie viele Trichter maximal angezeigt werden.")
		.icon(Material.HOPPER)
		.min(1)
		.defaultValue(1);

	private static final BooleanSetting showLastHopper = new BooleanSetting()
		.name("Letzte Trichter anzeigen")
		.description("Markiert die Trichter, die als letztes geöffnet wurden.")
		.icon(Material.WATCH)
		.subSettings(lastHoppersLimit);

	private static final BooleanSetting sneakMode = new BooleanSetting()
		.name("Sneak-Modus")
		.description("Öffnet bei Rechtsklicks immer die Einstellungen eines Trichters, auch wenn du nicht sneakst")
		.icon("sneaking")
		.addHotkeySetting("den Sneak-Modus", null);

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Trichter verbessern")
		.description("Verbessert Trichter.")
		.icon(Material.HOPPER)
		.subSettings(betterVisualisation, showRange, showSourceHopper, showLastHopper, sneakMode);

	private static final List<BlockPos> lastClickedHoppers = new ArrayList<>();

	public BetterHopper() {
		lastHoppersLimit.callback(i -> {
			lastHoppersLimit.icon(new ItemStack(Blocks.hopper, i));
			while (i < lastClickedHoppers.size())
				lastClickedHoppers.remove(0);
		});
	}

	@EventListener
	private static void onBlockInteract(BlockInteractEvent event) {
		if (world().getBlockState(event.pos).getBlock() != Blocks.hopper || player().getHeldItem() != null)
			return;

		if (player().isSneaking()) {
			addHopper(event.pos);
			return;
		}

		if (!sneakMode.get() || !enabled.get())
			return;

		addHopper(event.pos);
		mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), START_SNEAKING));
		TickScheduler.runAfterRenderTicks(() -> mc().getNetHandler().addToSendQueue(new C0BPacketEntityAction(player(), STOP_SNEAKING)), 1);
	}

	private static void addHopper(BlockPos pos) {
		lastClickedHoppers.remove(pos);
		lastClickedHoppers.add(pos);
		if (lastClickedHoppers.size() > lastHoppersLimit.get())
			lastClickedHoppers.remove(0);
	}

	@EventListener
	private void onBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
		if (event.packet.getBlockState().getBlock() != Blocks.hopper)
			lastClickedHoppers.remove(event.packet.getBlockPosition());
	}

	@EventListener
	private void onRenderTick(RenderWorldLastEvent event) {
		if (!showLastHopper.get() || lastClickedHoppers.isEmpty())
			return;

		double color = 192 / (float) lastClickedHoppers.size();
		for (int i = 0; i < lastClickedHoppers.size();) {
			BlockPos lastClickedHopper = lastClickedHoppers.get(i);
			AxisAlignedBB bb = new AxisAlignedBB(lastClickedHopper, lastClickedHopper.add(1, 1, 1)).expand(0.001, 0.001, 0.001);
			RenderUtil.drawFilledBox(bb, new Color(0, (int) (++i * color) + 63, 0, 0x80), false);
		}
	}

}
