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

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.tool_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MouseClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

/**
 * Suppresses clicks if the durability of the held item falls below the set threshold.
 */
@Singleton
public class ToolSaver extends ItemSaver {

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

	@MainElement
	final SwitchSetting enabled = SwitchSetting.create()
		.name("Werkzeug-Saver")
		.description("Verhindert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("broken_pickaxe")
		.subSettings(damage, saveNonRepairable);

	private GuiScreen previousScreen = null;

	@EventListener
	private void onLeftClick(MouseClickEvent.LeftClickEvent event) {
		if (player() != null && shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onRightClick(MouseClickEvent.RightClickEvent event) {
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
	private void onPacketSend(PacketSendEvent<Packet<?>> event) {
		if (event.packet instanceof C02PacketUseEntity) {
			if (shouldCancel(player().getHeldItem()))
				event.cancel();

			return;
		}

		if (event.packet instanceof C07PacketPlayerDigging) {
			C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
			if (packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && shouldCancel(player().getHeldItem()))
				event.cancel();

			return;
		}

		if (event.packet instanceof C08PacketPlayerBlockPlacement) {
			C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.packet;
			IBlockState state = world().getBlockState(packet.getPosition());
			if (shouldCancel(packet.getStack()) && (state == null || !(state.getBlock() instanceof BlockContainer)))
				event.cancel();
		}
	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();

		if (!event.itemStack.isItemStackDamageable()) {
			LabyBridge.labyBridge.notifyMildError("Dieses Item ist nicht vom Werkzeug-Saver betroffen!");
			return;
		}

		if (isExcluded(event.itemStack)) {
			LabyBridge.labyBridge.notifyMildError("Dieses Item ist bereits ausgenommen!");
			return;
		}

		String name = event.itemStack.getDisplayName();
		List<BaseSetting<?>> settings = enabled.getSubSettings();
		settings.add(settings.size() - 1, new ItemDisplaySetting(name, prepareStack(event.itemStack)));
		onChange();
	}

	@Override
	public void init() {
		super.init();
		List<BaseSetting<?>> settings = enabled.getSubSettings();

		settings.add(HeaderSetting.create("Ausnahmen"));

		if (Config.has(getConfigKey() + ".exclusions")) {
			JsonObject entries = Config.get(getConfigKey() + ".exclusions").getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : entries.entrySet())
				settings.add(new ItemDisplaySetting(entry.getKey(), ItemUtil.fromNBT(entry.getValue().getAsString())));
		}

		settings.add(EntryAddSetting.create()
			.name("Item hinzufügen")
			.callback(() -> {
				if (mc().thePlayer == null) {
					LabyBridge.labyBridge.notifyMildError("Hinzufügen von Ausnahmen ist nur Ingame möglich!");
					return;
				}

				previousScreen = mc().currentScreen;
				display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du als Ausnahme hinzufügen möchtest.");
				mc().displayGuiScreen(null);
			}));
	}

	// Required because when you break multiple blocks at once, the MouseEvent
	// is only triggered once, but the held item can be damaged multiple times
	@EventListener
	public void onTick(ClientTickEvent event) {
		if (player() == null || !shouldCancel(player().getHeldItem()))
			return;

		KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc().gameSettings.keyBindAttack.getKeyCode(), false);
	}

	public boolean shouldCancel(ItemStack heldItem) {
		if (heldItem == null || !heldItem.isItemStackDamageable())
			return false;

		if (isExcluded(heldItem))
			return false;

		if  (!ItemUtil.canBeRepaired(heldItem) && !saveNonRepairable.get())
			return false;

		return damage.get() >= heldItem.getMaxDamage() - heldItem.getItemDamage();
	}

	void onChange() {
		if (mc().currentScreen != null)
			mc().currentScreen.initGui();

		JsonObject object = new JsonObject();
		for (BaseSetting<?> element : enabled.getSubSettings()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting ids = (ItemDisplaySetting) element;
			object.addProperty(ids.name, ItemUtil.serializeNBT(ids.stack));
		}

		Config.set(getConfigKey() + ".exclusions", object);
		Config.save();
	}

	private boolean isExcluded(ItemStack stack) {
		if (stack == null)
			return false;

		ListIterator<BaseSetting<?>> iterator = enabled.getSubSettings().listIterator();
		String nbt = ItemUtil.serializeNBT(prepareStack(stack));

		while (iterator.hasNext()) {
			BaseSetting<?> element = iterator.next();

			if (element instanceof ItemDisplaySetting)
				if (nbt.equals(ItemUtil.serializeNBT(((ItemDisplaySetting) element).getStack())))
					return true;

			if (element instanceof EntryAddSetting)
				break;
		}

		return false;
	}

	private ItemStack prepareStack(ItemStack stack) {
		ItemStack is = stack.copy();
		is.stackSize = 1;

		if (is.isItemStackDamageable())
			is.setItemDamage(0);

		if (is.hasTagCompound()) {
			is.getTagCompound().removeTag("display");
			is.getTagCompound().removeTag("RepairCost");
			if (is.getTagCompound().hasNoTags())
				is.setTagCompound(null);
		}

		return is;
	}

}
