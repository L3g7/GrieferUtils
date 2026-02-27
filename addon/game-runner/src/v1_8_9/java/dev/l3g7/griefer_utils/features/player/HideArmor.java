/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Singleton
public class HideArmor extends Feature {

	private final SwitchSetting helmet = SwitchSetting.create()
		.name("Helme verstecken")
		.icon("diamond_helmet")
		.defaultValue(true);
	private final SwitchSetting chestplate = SwitchSetting.create()
		.name("Brustpanzer verstecken")
		.icon("diamond_chestplate")
		.defaultValue(true);
	private final SwitchSetting leggings = SwitchSetting.create()
		.name("Hosen verstecken")
		.icon("diamond_leggings")
		.defaultValue(true);
	private final SwitchSetting boots = SwitchSetting.create()
		.name("Schuhe verstecken")
		.icon("diamond_boots")
		.defaultValue(true);

	private final SwitchSetting[] subsettings = new SwitchSetting[]{helmet, chestplate, leggings, boots};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Rüstung verstecken")
		.description("Versteckt angezogene Rüstungen von Spielern.")
		.icon("diamond_chestplate")
		.subSettings(subsettings)
		.since("2.4-BETA-1");

	public static HideArmor get() {
		return get(HideArmor.class);
	}

	public static boolean shouldRender(int index) {
		if (index < 1 || index > 4)
			return true;

		if (!HideArmor.get().isEnabled())
			return true;

		return !HideArmor.get().subsettings[4 - index].get();
	}

	@Mixin(LayerArmorBase.class)
	private static class MixinLayerArmorBase {

		@Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
		private void injectRenderLayer(EntityLivingBase lvt_1_1_, float lvt_2_1_, float lvt_3_1_, float lvt_4_1_, float lvt_5_1_, float lvt_6_1_, float lvt_7_1_, float lvt_8_1_, int lvt_9_1_, CallbackInfo ci) {
			if (lvt_1_1_ instanceof EntityPlayer && !shouldRender(lvt_9_1_))
				ci.cancel();
		}

	}

}
