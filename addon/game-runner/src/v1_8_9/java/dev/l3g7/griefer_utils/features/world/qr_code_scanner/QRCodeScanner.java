/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.qr_code_scanner;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.Decoder;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.ModTextField;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.ItemSearch;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.image.BufferedImage;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY;

@Singleton
public class QRCodeScanner extends Feature {

	private static final Map<DecodeHintType, ?> DECODE_HINTS = ImmutableMap.of(DecodeHintType.TRY_HARDER, Boolean.TRUE);
	public static boolean suppressCorrectionErrors = false;

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

			LuminanceSource source = new BufferedImageLuminanceSource(img);
			LuminanceSource inverted = source.invert();
			try {
				if (scan(source) && scan(inverted) && scanAggressively(img)) // Adding DecodeHintType.ALSO_INVERTED doesn't suffice, for some reason
					display(ADDON_PREFIX + "§cEs konnte kein QR-Code gefunden werden.");
			} catch (FormatException e) {
				display(ADDON_PREFIX + "§cDas Format des QR-Codes ist ungültig.");
			} catch (ChecksumException e) {
				display(ADDON_PREFIX + "§eUngültige Fehlerkorrektur, Ergebnis kann fehlerhaft sein.");
				suppressCorrectionErrors = true;
				if (scan(source) && scan(inverted) && scanAggressively(img)) {
					display(ADDON_PREFIX + "§cEs konnte kein QR-Code gefunden werden.");
					display(ADDON_PREFIX + "§cWie hast du das geschafft? Bitte melde dich beim GrieferUtils-Team.");
				}

				suppressCorrectionErrors = false;
			}
		});

	private static boolean scanAggressively(BufferedImage img) throws ChecksumException, FormatException {
		AggressiveBinarizer[] binarizers = AggressiveBinarizer.get(img);
		if (binarizers == null) // Map only has one color
			return true;

		return scan(binarizers[0]) && scan(binarizers[1]);
	}

	private static boolean scan(LuminanceSource source) throws ChecksumException, FormatException {
		return scan(new HybridBinarizer(source));
	}

	private static boolean scan(Binarizer binarizer) throws ChecksumException, FormatException {
		try {
			display(ADDON_PREFIX + "QR-Code-Text: " + new QRCodeReader().decode(new BinaryBitmap(binarizer), DECODE_HINTS).getText());
			return false;
		} catch (NotFoundException e) {
			return true;
		}
	}

	@Mixin(value = Decoder.class, remap = false)
	private static abstract class MixinDecoder {

		@Shadow
		protected abstract void correctErrors(byte[] codewordBytes, int numDataCodewords) throws ChecksumException;

		@Redirect(method = "decode(Lcom/google/zxing/qrcode/decoder/BitMatrixParser;Ljava/util/Map;)Lcom/google/zxing/common/DecoderResult;", at = @At(value = "INVOKE", target = "Lcom/google/zxing/qrcode/decoder/Decoder;correctErrors([BI)V"))
	    private void injectCorrectErrors(Decoder instance, byte[] codewordBytes, int numDataCodewords) throws ChecksumException {
			try {
				correctErrors(codewordBytes, numDataCodewords);
			} catch (ChecksumException e) {
				if (!suppressCorrectionErrors)
					throw e;
			}
	    }

	}

}
