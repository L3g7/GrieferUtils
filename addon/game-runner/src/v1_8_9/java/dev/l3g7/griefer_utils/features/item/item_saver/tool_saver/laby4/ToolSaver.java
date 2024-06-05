/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.LeftClickEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.RightClickEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.features.item.recraft.TempToolSaverBridge;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

/**
 * Suppresses clicks if the durability of the held item falls below the set threshold.
 */
@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ToolSaver extends ItemSaver implements TempToolSaverBridge {

	private final NumberSetting damage = NumberSetting.create()
		.name("Min. Haltbarkeit")
		.description("Wenn ein Werkzeug diese Haltbarkeit erreicht hat, werden Klicks damit verhindert."
			+ "\nEs wird ein Wert von §nmindestens§r 3 empfohlen, damit das Item auch bei starken Lags nicht zerstört wird.")
		.icon("shield_with_sword")
		.defaultValue(3);

	private final SwitchSetting saveNonRepairable = SwitchSetting.create()
		.name("Irreparables retten")
		.description("Ob Items, die nicht mehr repariert werden können, auch gerettet werden sollen.")
		.icon("broken_pickaxe")
		.defaultValue(true);

	private final ToolProtectionListSetting exclusions = new ToolProtectionListSetting()
		.name("Ausnahmen")
		.disableSubsettingConfig()
		.icon(Items.golden_pickaxe);

	@MainElement
	final SwitchSetting enabled = SwitchSetting.create()
		.name("Werkzeug-Saver")
		.description("Verhindert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("broken_pickaxe")
		.subSettings(damage, saveNonRepairable, exclusions);

	@EventListener
	private void onLeftClick(LeftClickEvent event) {
		if (player() != null && shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onRightClick(RightClickEvent event) {
		if (player() == null)
			return;

		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop != null && mop.typeOfHit == BLOCK) {
			IBlockState state = world().getBlockState(mop.getBlockPos());
			if (state != null && state.getBlock() instanceof BlockContainer)
				return;
		}

		if (shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onPacketDigging(PacketSendEvent<C07PacketPlayerDigging> event) {
		if (event.packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onPacketUseEntity(PacketSendEvent<C02PacketUseEntity> event) {
		if (shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onPacketPlaceBlock(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		IBlockState state = world().getBlockState(event.packet.getPosition());
		if (shouldCancel(event.packet.getStack()) && (state == null || !(state.getBlock() instanceof BlockContainer)))
			event.cancel();
	}

	// Required because when you break multiple blocks at once, the MouseEvent
	// is only triggered once, but the held item can be damaged multiple times
	@EventListener
	private void onTick(ClientTickEvent event) {
		if (player() == null || !shouldCancel(player().getHeldItem()))
			return;

		KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc().gameSettings.keyBindAttack.getKeyCode(), false);
	}

	public boolean shouldCancel(ItemStack heldItem) {
		if (heldItem == null || !heldItem.isItemStackDamageable())
			return false;

		if (exclusions.isExcluded(heldItem))
			return false;

		if (!ItemUtil.canBeRepaired(heldItem) && !saveNonRepairable.get())
			return false;

		return damage.get() >= heldItem.getMaxDamage() - heldItem.getItemDamage();
	}

}
