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

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * Suppresses clicks if the durability of the held item falls below the set threshold.
 */
@Singleton
public class ToolSaver extends ItemSaver {

	private final NumberSetting damage = new NumberSetting()
		.name("Min. Haltbarkeit")
		.description("Wenn ein Werkzeug diese Haltbarkeit erreicht hat, werden Klicks damit verhindert."
			+ "\nEs wird ein Wert von §nmindestens§r 3 empfohlen, damit das Item auch bei starken Lags nicht zerstört wird.")
		.icon("shield_with_sword")
		.defaultValue(3);

	private final BooleanSetting saveNonRepairable = new BooleanSetting()
		.name("Irreparables retten")
		.description("Ob Items, die nicht mehr repariert werden können, auch gerettet werden sollen.")
		.icon("broken_pickaxe")
		.defaultValue(true);

	@MainElement
	final BooleanSetting enabled = new BooleanSetting()
		.name("Werkzeug-Saver")
		.description("Verhindert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.")
		.icon("broken_pickaxe")
		.subSettings(damage, saveNonRepairable);

	private GuiScreen previousScreen = null;

	@EventListener
	public void onMouse(MouseClickEvent event) {
		if (isEnabled() && player() == null || shouldCancel(player().getHeldItem()))
			event.cancel();
	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();

		if (!event.itemStack.isItemStackDamageable()) {
			displayAchievement("§e§lFehler \u26A0", "§eDieses Item ist nicht vom Werkzeug-Saver betroffen!");
			return;
		}

		if (isExcluded(event.itemStack)) {
			displayAchievement("§e§lFehler \u26A0", "§eDieses Item ist bereits ausgenommen!");
			return;
		}

		String name = event.itemStack.getDisplayName();
		List<SettingsElement> settings = enabled.getSubSettings().getElements();
		settings.add(settings.size() - 1, new ItemDisplaySetting(name, prepareStack(event.itemStack)));
		onChange();
	}

	@Override
	public void init() {
		super.init();
		List<SettingsElement> settings = enabled.getSubSettings().getElements();

		settings.add(new HeaderSetting("Ausnahmen"));

		if (Config.has(getConfigKey() + ".exclusions")) {
			JsonObject entries = Config.get(getConfigKey() + ".exclusions").getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : entries.entrySet())
				settings.add(new ItemDisplaySetting(entry.getKey(), ItemUtil.fromNBT(entry.getValue().getAsString())));
		}

		settings.add(new EntryAddSetting()
			.name("Item hinzufügen")
			.callback(() -> {
				if (mc().thePlayer == null) {
					displayAchievement("§e§lFehler \u26A0", "§eHinzufügen von Ausnahmen ist nur Ingame möglich!");
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
		if (!isEnabled() || player() == null || !shouldCancel(player().getHeldItem()))
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
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting ids = (ItemDisplaySetting) element;
			object.addProperty(ids.name, ids.stack.serializeNBT().toString());
		}

		Config.set(getConfigKey() + ".exclusions", object);
		Config.save();
	}

	private boolean isExcluded(ItemStack stack) {
		if (stack == null)
			return false;

		ListIterator<SettingsElement> iterator = enabled.getSubSettings().getElements().listIterator();
		String nbt = prepareStack(stack).serializeNBT().toString();

		while (iterator.hasNext()) {
			SettingsElement element = iterator.next();

			if (element instanceof ItemDisplaySetting)
				if (nbt.equals(((ItemDisplaySetting) element).getStack().serializeNBT().toString()))
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
