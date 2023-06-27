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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SliderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

/**
 * Shows invisible entities.
 */
@Singleton
public class TrueSight extends Feature {

	private static final TrueSight INSTANCE = FileProvider.getSingleton(TrueSight.class);

	private final SliderSetting opacity = new SliderSetting()
		.name("Durchsichtigkeit (%)")
		.icon("fading_steve")
		.min(0).max(100)
		.defaultValue(85);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("TrueSight")
		.description("Macht unsichtbare Entities sichtbar.")
		.icon("blue_light_bulb")
		.subSettings(opacity, new HeaderSetting());

	private final Map<Class<? extends Entity>, BooleanSetting> entities = new HashMap<>();

	@Override
	public void init() {
		super.init();
		add(EntityArmorStand.class, "Armorstand", "armor_stand");
		add(EntityBat.class, "Fledermaus", "bat");
		add(EntityBlaze.class, "Blaze", "blaze");
		add(EntityCaveSpider.class, "Höhlenspinne", "cave_spider");
		add(EntityChicken.class, "Huhn", "chicken");
		add(EntityCow.class, "Kuh", "cow");
		add(EntityCreeper.class, "Creeper", "creeper");
		add(EntityDragon.class, "Enderdrache", "ender_dragon");
		add(EntityEnderman.class, "Enderman", "enderman");
		add(EntityEndermite.class, "Endermite", "endermite");
		entities.put(EntityFallingBlock.class, new BooleanSetting()
			.name("Block")
			.config(getConfigKey() + ".entities.falling_block")
			.icon("stone"));
		add(EntityGhast.class, "Ghast", "ghast");
		add(EntityGiantZombie.class, "Riese", "zombie");
		add(EntityGuardian.class, "Guardian", "guardian");
		add(EntityHorse.class, "Pferd", "horse");
		add(EntityIronGolem.class, "Eisengolem", "iron_golem");
		add(EntityMagmaCube.class, "Magmawürfel", "magma_cube");
		add(EntityMooshroom.class, "Pilzkuh", "mooshroom");
		add(EntityOcelot.class, "Ozelot", "ocelot");
		add(EntityPig.class, "Schwein", "pig");
		add(EntityPigZombie.class, "Schweinezombie", "pig_zombie");
		add(EntityPlayer.class, "Spieler", "../steve");
		add(EntityRabbit.class, "Hase", "rabbit");
		add(EntitySheep.class, "Schaf", "sheep");
		add(EntitySilverfish.class, "Silberfischchen", "silverfish");
		add(EntitySkeleton.class, "Skelett", "skeleton");
		add(EntitySlime.class, "Slime", "slime");
		add(EntitySnowman.class, "Schneegolem", "snow_golem");
		add(EntitySpider.class, "Spinne", "spider");
		add(EntitySquid.class, "Tintenfisch", "squid");
		add(EntityVillager.class, "Dorfbewohner", "villager");
		add(EntityWitch.class, "Hexe", "witch");
		add(EntityWolf.class, "Wolf", "wolf");
		add(EntityZombie.class, "Zombie", "zombie");

		List<SettingsElement> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(SettingsElement::getDisplayName));
		enabled.subSettings(settings);
	}

	private void add(Class<? extends Entity> entity, String name, String texture) {
		entities.put(entity, new BooleanSetting()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon("mob_icons/" + texture)
			.defaultValue(entity == EntityPlayer.class));
	}

	@EventListener
	public void onDisplayNameRender(InvisibilityCheckEvent event) {
		Class<?> entity = event.entity.getClass();
		do {
			if (entities.containsKey(entity)) {
				event.setCanceled(entities.get(entity).get());
				break;
			}
		} while ((entity = entity.getSuperclass()) != null);
	}

	public static float getRenderModelAlpha() {
		if(!INSTANCE.isEnabled())
			return 0.15f;

		return 0.01f * (100f - INSTANCE.opacity.get());
	}

}
