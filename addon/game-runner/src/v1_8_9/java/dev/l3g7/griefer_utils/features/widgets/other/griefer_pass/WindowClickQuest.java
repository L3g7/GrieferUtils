/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceivedEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public abstract class WindowClickQuest extends AbstractQuest {

	private final Map<Short, Pair<C0EPacketClickWindow, Container>> pendingConfirmation = new HashMap<>();
	private String lastWindowTitle = null;

	@EventListener
	private void onClickWindow(PacketSendEvent<C0EPacketClickWindow> event) {
		pendingConfirmation.put(event.packet.getActionNumber(), new Pair<>(event.packet, player().openContainer));
		lastWindowTitle = MinecraftUtil.getGuiChestTitle(mc().currentScreen);
	}

	@EventListener
	private void onWindowTransform(PacketReceivedEvent<S32PacketConfirmTransaction> event) {
		Pair<C0EPacketClickWindow, Container> confirm = pendingConfirmation.remove(event.packet.getActionNumber());
		if (confirm != null && onClickWindow(confirm.a, confirm.b))
			increaseAmount();
	}

	@EventListener
	private void onWorldUnload(WorldUnloadEvent event) {
		pendingConfirmation.clear();
	}

	protected abstract boolean onClickWindow(C0EPacketClickWindow packet, Container container);

	static class CraftQuest extends WindowClickQuest {
		@Override
		protected boolean onClickWindow(C0EPacketClickWindow packet, Container container) {
			if (packet.getClickedItem() == null)
				return false;

			if (!(container instanceof ContainerPlayer) && !(container instanceof ContainerWorkbench))
				return false;

			return packet.getSlotId() == 0 || packet.getSlotId() == 1 || packet.getSlotId() == 2;

		}
	}

	static class EnchantQuest extends AbstractQuest {

		private boolean pressedButton = false;

		@EventListener
		private void onClickButton(PacketSendEvent<C11PacketEnchantItem> event) {
			pressedButton = true;
		}

		@EventListener
		private void onClose(PacketSendEvent<C0DPacketCloseWindow> event) {
			pressedButton = false;
		}

		@EventListener
		private void onSetXP(PacketReceiveEvent<S1FPacketSetExperience> event) {
			if (pressedButton) {
				pressedButton = false;
				increaseAmount();
			}
		}

	}

	static class SpawnerQuest extends AbstractQuest {
		int lastStackSize = -1;

		@EventListener
		private void onClickWindow(PacketSendEvent<C0EPacketClickWindow> event) {
			lastStackSize = -1;
			if (event.packet.getSlotId() > 53)
				return;

			String title = MinecraftUtil.getGuiChestTitle(mc().currentScreen);
			if (!title.startsWith("§6Spawner - Lager") || event.packet.getClickedItem() == null)
				return;

			int column = event.packet.getSlotId() % 9;
			if (event.packet.getClickedItem().getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
				|| column == 0 || column == 8)
				return; // No not an item

			increaseAmount(lastStackSize = event.packet.getClickedItem().stackSize);
		}

		@EventListener
		private void onMessageReceive(MessageReceiveEvent event) {
			if (lastStackSize != -1 && event.message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§cDu hast keinen freien Platz im Inventar.§r"))
				increaseAmount(-lastStackSize);
		}
	}

}
