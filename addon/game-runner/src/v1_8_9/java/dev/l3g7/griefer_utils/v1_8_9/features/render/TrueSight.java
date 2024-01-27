/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.*;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

/**
 * Shows invisible entities.
 */
@Singleton
public class TrueSight extends Feature {

	private static final TrueSight INSTANCE = FileProvider.getSingleton(TrueSight.class);
	private static final Map<Class<?>, String> CLASS_TO_STRING_MAPPING = Reflection.get(EntityList.class, "classToStringMapping");

	private final SliderSetting opacity = SliderSetting.create()
		.name("Durchsichtigkeit (%)")
		.description("Wie durchsichtig ein eigentlich unsichtbares Entity sein soll.")
		.icon("fading_steve")
		.min(0).max(100)
		.defaultValue(85);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Unsichtbare Entities anzeigen")
		.description("Macht unsichtbare Entities sichtbar.")
		.icon("blue_light_bulb")
		.subSettings(opacity, HeaderSetting.create());

	private final Map<Class<? extends Entity>, SwitchSetting> entities = new HashMap<>();

	@Override
	public void init() {
		super.init();
		add(EntityArmorStand.class, "Armorstand");
		add(EntityBat.class, "Fledermaus");
		add(EntityBlaze.class, "Blaze");
		add(EntityCaveSpider.class, "Höhlenspinne");
		add(EntityChicken.class, "Huhn");
		add(EntityCow.class, "Kuh");
		add(EntityCreeper.class, "Creeper");
		add(EntityDragon.class, "Enderdrache");
		add(EntityEnderman.class, "Enderman");
		add(EntityEndermite.class, "Endermite");
		entities.put(EntityFallingBlock.class, SwitchSetting.create()
			.name("Block")
			.config(getConfigKey() + ".entities.falling_block")
			.icon("stone"));
		add(EntityGhast.class, "Ghast");
		add(EntityGiantZombie.class, "Riese");
		add(EntityGuardian.class, "Guardian");
		add(EntityHorse.class, "Pferd");
		add(EntityIronGolem.class, "Eisengolem");
		add(EntityMagmaCube.class, "Magmawürfel");
		add(EntityMooshroom.class, "Pilzkuh");
		add(EntityOcelot.class, "Ozelot");
		add(EntityPig.class, "Schwein");
		add(EntityPigZombie.class, "Schweinezombie");
		entities.put(EntityPlayer.class, SwitchSetting.create()
			.name("Spieler")
			.config(getConfigKey() + ".entities.spieler")
			.icon("steve"));
		add(EntityRabbit.class, "Hase");
		add(EntitySheep.class, "Schaf");
		add(EntitySilverfish.class, "Silberfischchen");
		add(EntitySkeleton.class, "Skelett");
		add(EntitySlime.class, "Slime");
		add(EntitySnowman.class, "Schneegolem");
		add(EntitySpider.class, "Spinne");
		add(EntitySquid.class, "Tintenfisch");
		add(EntityVillager.class, "Dorfbewohner");
		add(EntityWitch.class, "Hexe");
		add(EntityWolf.class, "Wolf");
		add(EntityZombie.class, "Zombie");

		List<BaseSetting<?>> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(BaseSetting::name));
		enabled.subSettings(settings);
	}

	private void add(Class<? extends Entity> entity, String name) {
		entities.put(entity, SwitchSetting.create()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon("mob_icons/faithless/" + CLASS_TO_STRING_MAPPING.get(entity).toLowerCase())
			.defaultValue(entity == EntityPlayer.class));
	}

	@EventListener
	public void onInvisibilityCheck(InvisibilityCheckEvent event) {
		Class<?> entity = event.entity.getClass();
		do {
			if (entities.containsKey(entity)) {
				event.invisible = !entities.get(entity).get();
				break;
			}
		} while ((entity = entity.getSuperclass()) != null);
	}

	public static float getRenderModelAlpha() {
		if(!INSTANCE.isEnabled())
			return 0.15f;

		return 0.01f * (100f - INSTANCE.opacity.get());
	}

	@Mixin(RendererLivingEntity.class)
	private static class MixinRendererLivingEntity {

		@ModifyArg(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"), index = 3)
		private float overwriteAlpha(float alpha) {
			return getRenderModelAlpha();
		}

	}

}
