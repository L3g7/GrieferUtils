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

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.ModTextField;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;

import java.awt.image.BufferedImage;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY;

@Singleton
public class QRCodeScanner extends Feature {

	@MainElement
	private final KeySetting enabled = KeySetting.create()
		.name("QR-Code Scanner")
		.description("Liest QR-Codes aus Karten in Rahmen oder Inventaren.")
		.icon("qr_code")
		.triggersInContainers()
		.pressCallback(pressed -> {
			if (!pressed)
				return;

			boolean isInGui = mc().currentScreen instanceof GuiInventory || mc().currentScreen instanceof GuiChest;
			if (mc().currentScreen != null && !isInGui)
				return;

			ItemStack stack;

			if (mc().currentScreen != null) {
				ModTextField mtf = FileProvider.getSingleton(ItemSearch.class).searchField;
				if (mtf != null && mtf.isFocused())
					return;

				stack = MinecraftUtil.getStackUnderMouse(mc().currentScreen);
				if (stack == null || stack.getItem() != Items.filled_map) {
					display(ADDON_PREFIX + "Bitte hovere eine Karte.");
					return;
				}
			} else if (mc().objectMouseOver == null || mc().objectMouseOver.typeOfHit != ENTITY
				|| !((mc().objectMouseOver.entityHit) instanceof EntityItemFrame)
				|| (stack = ((EntityItemFrame) mc().objectMouseOver.entityHit).getDisplayedItem()).getItem() != Items.filled_map) {
				display(ADDON_PREFIX + "Bitte schaue eine Karte an.");
				return;
			}

			MapData mapData = Items.filled_map.getMapData(stack, world());
			if (mapData == null) {
				display(ADDON_PREFIX + "§cDie Karte wurde noch nicht geladen.");
				return;
			}

			BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < 16384; ++i) {
				int colorId = mapData.colors[i] & 255;

				img.setRGB(i % 128, i / 128, colorId / 4 == 0
					? (i + i / 128 & 1) * 8 + 16 << 24
					: MapColor.mapColorArray[colorId / 4].getMapColor(colorId & 3));
			}

			/*
			TODO:
			try {
				BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(img);
				if (scan(source) && scan(source.invert())) // Adding DecodeHintType.ALSO_INVERTED doesn't suffice, for some reason
					display(ADDON_PREFIX + "§cEs konnte kein QR-Code gefunden werden.");
			} catch (ChecksumException e) {
				display(ADDON_PREFIX + "§cDie Prüfsumme des QR-Codes ist ungültig.");
			} catch (FormatException e) {
				display(ADDON_PREFIX + "§cDas Format des QR-Codes ist ungültig.");
			}*/
		});

/*
TODO:
	private static boolean scan(LuminanceSource source) throws ChecksumException, FormatException {
		BinaryBitmap data = new BinaryBitmap(new HybridBinarizer(source));
		try {
			display(ADDON_PREFIX + "QR-Code-Text: " + new QRCodeReader().decode(data).getText());
			return false;
		} catch (NotFoundException e) {
			return true;
		}
	}
*/
}
