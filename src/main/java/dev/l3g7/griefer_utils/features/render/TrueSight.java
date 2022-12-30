/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SliderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.skull;
import static net.minecraft.init.Items.*;

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
		.subSettings(opacity);

	private final Map<Class<? extends Entity>, BooleanSetting> entities = new HashMap<>();

	@Override
	public void init() {
		super.init();
		add(EntityArmorStand.class, "Armorstand", "mob_icons/armor_stand");
		add(EntityBat.class, "Fledermaus", "mob_icons/bat");
		add(EntityBlaze.class, "Blaze", "mob_icons/blaze");
		add(EntityCaveSpider.class, "Höhlenspinne", "mob_icons/cave_spider");
		add(EntityChicken.class, "Huhn", "mob_icons/chicken");
		add(EntityCow.class, "Kuh", "mob_icons/cow");
		add(EntityCreeper.class, "Creeper", "mob_icons/creeper");
		add(EntityDragon.class, "Enderdrache", "mob_icons/ender_dragon");
		add(EntityEnderman.class, "Enderman", "mob_icons/ender_man");
		add(EntityEndermite.class, "Endermite", "mob_icons/ender_mite");
		add(EntityFallingBlock.class, "FallingBlock", Material.STONE);
		add(EntityGhast.class, "Ghast", "mob_icons/ghast");
		add(EntityGiantZombie.class, "Riese", "mob_icons/zombie");
		add(EntityGuardian.class, "Guardian", "mob_icons/guardian");
		add(EntityHorse.class, "Pferd", "mob_icons/horse");
		add(EntityIronGolem.class, "Eisengolem", "mob_icons/iron_golem");
		add(EntityMagmaCube.class, "Magmawürfel", "mob_icons/lava_slime");
		add(EntityMooshroom.class, "Pilzkuh", "mob_icons/mushroom_cow");
		add(EntityOcelot.class, "Ozelot", "mob_icons/ocelot");
		add(EntityPig.class, "Schwein", "mob_icons/pig");
		add(EntityPigZombie.class, "Schweinezombie", "mob_icons/pig_zombie");
		add(EntityPlayer.class, "Spieler", "steve");
		add(EntityRabbit.class, "Hase", "mob_icons/rabbit");
		add(EntitySheep.class, "Schaf", "mob_icons/sheep");
		add(EntitySilverfish.class, "Silberfischchen", "mob_icons/silverfish");
		add(EntitySkeleton.class, "Skelett", "mob_icons/skeleton");
		add(EntitySlime.class, "Slime", "mob_icons/slime");
		add(EntitySnowman.class, "Schneegolem", "mob_icons/snow_golem");
		add(EntitySpider.class, "Spinne", "mob_icons/spider");
		add(EntitySquid.class, "Tintenfisch", "mob_icons/squid");
		add(EntityVillager.class, "Dorfbewohner", "mob_icons/villager");
		add(EntityWitch.class, "Hexe", "mob_icons/witch");
		add(EntityWolf.class, "Wolf", "mob_icons/wolf");
		add(EntityZombie.class, "Zombie", "mob_icons/zombie");

		List<SettingsElement> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(SettingsElement::getDisplayName));
		enabled.subSettings(settings);
	}

	private void add(Class<? extends Entity> entity, String name, Object icon) {
		entities.put(entity, new BooleanSetting()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon(icon)
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
