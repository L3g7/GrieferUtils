/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.better_schematica;

import com.github.lunatrius.schematica.client.gui.control.GuiSchematicMaterials;
import com.github.lunatrius.schematica.client.util.BlockList;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.SCHEMATICA;

@Singleton
@SuppressWarnings("unused")
public class BetterSchematica extends Feature {

	private final SwitchSetting highlightBlocks = SwitchSetting.create()
		.name("Ausgewählten Block hervorheben")
		.description("Markiert alle Blöcke vom selben Typ des in der Hand gehaltenen Items.")
		.icon("litematica/green_highlight");

	private final SwitchSetting savePosition = SwitchSetting.create()
		.name("\"Speichern\" Knopf")
		.description("Fügt in der Schematic-Kontrolle einen Knopf hinzu, der die derzeit geladene Schematic mit Drehung, Spiegelung und Position speichert."
			+ "\nWenn die Schematic geladen wird, wird sie automatisch an die gespeicherte Position geschoben.")
		.icon("litematica/axes");

	private final SwitchSetting openMaterialFile = SwitchSetting.create()
		.name("Material-Datei öffnen")
		.description("Öffnet die Material-Datei nach dem Speichern.")
		.icon("lectern");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Schematica verbessern")
		.description("Erleichtert das Arbeiten mit Schematica.")
		.icon("litematica/litematica")
		.subSettings(highlightBlocks, savePosition, openMaterialFile);

	public static BetterSchematica get() {
		return get(BetterSchematica.class);
	}

	@Override
	public void init() {
		super.init();

		if (!SCHEMATICA) {
			enabled.name("§7Schematica verbessern")
				.description("Verbessert Schematica.\n\n(Schematica ist nicht installiert.)")
				.set(false)
				.disable()
				.callback(b -> {
					if (b)
						enabled.set(false);
				});
			return;
		}

		highlightBlocks.callback(() -> {
			if (isEnabled())
				SchematicaUtil.refresh();
		});

		enabled.callback(() -> {
			if (highlightBlocks.get())
				SchematicaUtil.refresh();
		});

		getCategory().callback(() -> {
			if (enabled.get() && highlightBlocks.get())
				SchematicaUtil.refresh();
		});
	}

	@EventListener
	public void onRenderTick(RenderTickEvent event) {
		if (SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onRenderTick(event);
	}

	@EventListener
	public void onPacketSend(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		if (SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onPacketSend(event);
	}

	static boolean isHighlightBlocksEnabled() {
		if (!SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = BetterSchematica.get();
		return betterSchematica.isEnabled() && betterSchematica.highlightBlocks.get();
	}

	static boolean isSavePositionEnabled() {
		if (!SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = BetterSchematica.get();
		return betterSchematica.isEnabled() && betterSchematica.savePosition.get();
	}

	@Mixin(GuiSchematicMaterials.class)
	public static class GuiSchematicMaterialsMixin {

		@Inject(method = "dumpMaterialList", at = @At(value = "INVOKE", target = "Lorg/apache/commons/io/IOUtils;write(Ljava/lang/String;Ljava/io/OutputStream;)V", shift = At.Shift.AFTER))
		void injectDumpMaterialList(List<BlockList.WrappedItemStack> blockList, CallbackInfo ci) {
			BetterSchematica.openMaterialFile();
		}

		@Inject(method = "dumpMaterialList", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", shift = At.Shift.AFTER))
		void injectDumpMaterialListError(List<BlockList.WrappedItemStack> blockList, CallbackInfo ci) {
			BetterSchematica.writeErrorMessage();
		}

	}

	public static void openMaterialFile() {
		if (!SCHEMATICA)
			return;

		BetterSchematica betterSchematica = BetterSchematica.get();
		if (!betterSchematica.openMaterialFile.get())
			return;

		if (!labyBridge.openFile(SchematicaUtil.MATERIAL_FILE))
			labyBridge.notifyMildError("Datei konnte nicht geöffnet werden");
	}

	public static void writeErrorMessage() {
		if (!SCHEMATICA)
			return;

		BetterSchematica betterSchematica = BetterSchematica.get();
		if (betterSchematica.openMaterialFile.get())
			labyBridge.notifyError("Datei konnte nicht gespeichert werden");
	}

}
