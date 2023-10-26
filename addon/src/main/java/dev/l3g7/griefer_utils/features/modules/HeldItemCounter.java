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

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import net.labymod.ingamegui.moduletypes.ItemModule;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * This isn't a real module, it injects into LabyMod's {@link ItemModule}
 */
public class HeldItemCounter {

	public static BooleanSetting enabled = new BooleanSetting()
		.name("Item-Zähler")
		.description("Zeigt anstatt der Haltbarkeit an, wie viele Items von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("spyglass")
		.config("modules.held_item_counter.enabled");

	@Mixin(value = ItemModule.class, remap = false)
	private abstract static class MixinItemModule {

		@Inject(method = "fillSubSettings", at = @At("RETURN"))
		private void injectFillSubSettings(List<SettingsElement> settings, CallbackInfo ci) {
			if (classCheckFails())
				return;

			settings.add(new HeaderSetting().entryHeight(8));
			settings.add(new HeaderSetting("§r§l" + Constants.ADDON_NAME).scale(1.3));
			settings.add(new HeaderSetting().entryHeight(8));
			settings.add(enabled);

			// Copy settings of the original ItemCounter
			List<SettingsElement> subSettings = enabled.getSubSettings().getElements();
			if (subSettings.isEmpty()) {
				subSettings.add(new HeaderSetting().entryHeight(8));
				subSettings.add(new HeaderSetting("§r§l" + Constants.ADDON_NAME).scale(1.3));
				subSettings.add(new HeaderSetting("Item-Zähler"));
				subSettings.add(new HeaderSetting().entryHeight(8));
				List<SettingsElement> originalSettings = FileProvider.getSingleton(ItemCounter.class).mainElement.getSubSettings().getElements();
				subSettings.addAll(originalSettings.subList(originalSettings.size() - 3, originalSettings.size()));
			}
		}

		@Redirect(method = "draw(DDDLnet/labymod/ingamegui/enums/EnumItemSlot;)V", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/DrawUtils;drawItem(Lnet/minecraft/item/ItemStack;DDLjava/lang/String;)V"))
		private void drawItem(DrawUtils instance, ItemStack stack, double xPosition, double yPosition, String value) {
			if (classCheckFails() || !enabled.get() || !stack.isItemStackDamageable()) {
				instance.drawItem(stack, xPosition, yPosition, null);
				return;
			}

			List<ItemStack> itemStacks = Arrays.asList(player().inventory.mainInventory);
			int totalAmount = FileProvider.getSingleton(ItemCounter.class).getAmount(itemStacks, stack);
			instance.drawItem(stack, xPosition, yPosition, Constants.DECIMAL_FORMAT_98.format(totalAmount));
		}

		@SuppressWarnings("ConstantValue")
		private boolean classCheckFails() {
			return !((Object) this instanceof ItemModule);
		}

	}

}
