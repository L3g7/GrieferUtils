/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.BlockEvent.BlockClickEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.RightClickEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

/**
 * Suppresses clicks if the durability of the held item falls below the set threshold.
 */
@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ToolSaver extends Feature {

	private final NumberSetting damage = NumberSetting.create()
		.name("Min. Haltbarkeit")
		.description("Wenn ein Werkzeug diese Haltbarkeit erreicht hat, werden Klicks damit verhindert."
			+ "\nEs wird ein Wert von §nmindestens§r 3 empfohlen, damit das Item auch bei starken Lags nicht zerstört wird.")
		.icon("shield_with_sword")
		.defaultValue(3);

	private final SwitchSetting saveNonRepairable = SwitchSetting.create()
		.name("Irreparables retten")
		.description("Ob Items, die nicht mehr repariert werden können, auch gerettet werden sollen.")
		.icon("weakness")
		.defaultValue(true);

	private final ListSetting<ToolExclusion> exclusions = ListSetting.create(ToolExclusion::new)
		.name("Ausnahmen")
		.description("Werkzeuge, die nicht vor Zerstörung geschützt werden sollen.")
		.icon("weakness")
		.customAdd(this::addProtectedItem);

	@MainElement
	final SwitchSetting enabled = SwitchSetting.create()
		.name("Werkzeug-Saver")
		.description("Verhindert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("tools")
		.subSettings(damage, saveNonRepairable, exclusions);

	// Item add logic

	/**
	 * The screen that triggered the current tool add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	private void addProtectedItem() {
		if (mc().thePlayer == null) {
			labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur ingame möglich!");
			return;
		}

		previousScreen = mc().currentScreen;
		display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
		MinecraftUtil.closeClientsideGUI();
	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();

		if (!event.itemStack.isItemStackDamageable()) {
			labyBridge.notify("§e§lFehler ⚠", "§eDieses Item ist nicht vom Werkzeug-Saver betroffen!");
			return;
		}

		if (isExcluded(event.itemStack)) {
			labyBridge.notify("§e§lFehler ⚠", "§eDieses Item ist bereits ausgenommen!");
			return;
		}

		exclusions.add(new ToolExclusion(event.itemStack));
		exclusions.notifyChange();
	}

	// Helpers

	public boolean isExcluded(ItemStack stack) {
		if (stack == null)
			return false;

		String nbt = ItemUtil.serializeNBT(ToolExclusion.normalizeStack(stack));

		for (ToolExclusion protection : exclusions.get()) // NOTE: better NBT comparison
			if (nbt.equals(ItemUtil.serializeNBT(protection.getStack())))
				return true;

		return false;
	}

	public static boolean shouldProtect(ItemStack heldItem) {
		return get(ToolSaver.class).shouldProtectItem(heldItem);
	}

	public boolean shouldProtectItem(ItemStack heldItem) {
		if (!isEnabled())
			return false;

		if (heldItem == null || !heldItem.isItemStackDamageable())
			return false;

		if (isExcluded(heldItem))
			return false;

		if (!ItemUtil.canBeRepaired(heldItem) && !saveNonRepairable.get())
			return false;

		return damage.get() >= heldItem.getMaxDamage() - heldItem.getItemDamage();
	}

	// Protections

	@EventListener
	private void denyLeftClick(BlockClickEvent event) {
		if (player() != null && shouldProtectItem(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void denyRightClick(RightClickEvent event) {
		if (player() == null)
			return;

		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop != null && mop.typeOfHit == BLOCK) {
			IBlockState state = world().getBlockState(mop.getBlockPos());
			if (state != null && state.getBlock() instanceof BlockContainer)
				return;
		}

		if (shouldProtectItem(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void denyDigging(PacketSendEvent<C07PacketPlayerDigging> event) {
		if (event.packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && shouldProtectItem(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void denyUseEntity(PacketSendEvent<C02PacketUseEntity> event) {
		if (shouldProtectItem(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void denyPlaceBlock(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		IBlockState state = world().getBlockState(event.packet.getPosition());
		if (shouldProtectItem(event.packet.getStack()) && (state == null || !(state.getBlock() instanceof BlockContainer)))
			event.cancel();
	}

}
