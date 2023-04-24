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
		entities.put(EntityFallingBlock.class, new BooleanSetting()
			.name("FallingBlock")
			.config(getConfigKey() + ".entities.falling_block")
			.icon(Material.STONE));
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
		add(EntityPlayer.class, "Spieler");
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

		List<SettingsElement> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(SettingsElement::getDisplayName));
		enabled.subSettings(settings);
	}

	private void add(Class<? extends Entity> entity, String name) {
		String key = EntityList.classToStringMapping.getOrDefault(entity, "../glitch_question_mark");
		if (entity == EntityPlayer.class)
			key = "../steve";

		entities.put(entity, new BooleanSetting()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon("mob_icons/" + key)
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
