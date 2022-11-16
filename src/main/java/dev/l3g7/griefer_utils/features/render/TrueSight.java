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
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SliderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.util.reflection.Reflection.set;

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
		addEntities(
			EntityArmorStand.class, "Armorstand", Items.armor_stand,
			EntityBat.class, "Fledermaus", Items.spawn_egg, 65,
			EntityBlaze.class, "Blaze", Items.spawn_egg, 161,
			EntityCaveSpider.class, "Höhlenspinne", Items.spawn_egg, 59,
			EntityChicken.class, "Huhn", Items.spawn_egg, 93,
			EntityCow.class, "Kuh", Items.spawn_egg, 92,
			EntityCreeper.class, "Creeper", Items.spawn_egg, 50,
			EntityDragon.class, "Enderdrache", Blocks.dragon_egg,
			EntityEnderman.class, "Enderman", Items.spawn_egg, 58,
			EntityEndermite.class, "Endermite", Items.spawn_egg, 67,
			EntityFallingBlock.class, "FallingBlock", Blocks.sand,
			EntityGhast.class, "Ghast", Items.spawn_egg, 56,
			EntityGiantZombie.class, "Riese", Items.spawn_egg, 54,
			EntityGuardian.class, "Guardian", Items.spawn_egg, 68,
			EntityHorse.class, "Pferd", Items.spawn_egg, 100,
			EntityIronGolem.class, "Eisengolem", Blocks.iron_block,
			EntityMagmaCube.class, "Magmawürfel", Items.spawn_egg, 62,
			EntityMooshroom.class, "Pilzkuh", Items.spawn_egg, 96,
			EntityOcelot.class, "Ozelot", Items.spawn_egg, 98,
			EntityPig.class, "Schwein", Items.spawn_egg, 90,
			EntityPigZombie.class, "Schweinezombie", Items.spawn_egg, 57,
			EntityPlayer.class, "Spieler", Items.skull, 3,
			EntityRabbit.class, "Hase", Items.spawn_egg, 101,
			EntitySheep.class, "Schaf", Items.spawn_egg, 91,
			EntitySilverfish.class, "Silberfischchen", Items.spawn_egg, 60,
			EntitySkeleton.class, "Skelett", Items.spawn_egg, 51,
			EntitySlime.class, "Slime", Items.spawn_egg, 55,
			EntitySnowman.class, "Schneegolem", Items.snowball,
			EntitySpider.class, "Spinne", Items.spawn_egg, 52,
			EntitySquid.class, "Tintenfisch", Items.spawn_egg, 94,
			EntityVillager.class, "Dorfbewohner", Items.spawn_egg, 120,
			EntityWitch.class, "Hexe", Items.spawn_egg, 66,
			EntityWolf.class, "Wolf", Items.spawn_egg, 95,
			EntityZombie.class, "Zombie", Items.spawn_egg, 54
		);
	}

	/**
	 * @param args: Consisting of
	 *            - entityType
	 *            - entity name
	 *            - icon item
	 *            - optional item damage
	 */
	private void addEntities(Object... args) {
		for (int i = 0; i < args.length; i += 3) {
			try {
				Class<? extends Entity> entity = c(args[i]);
				int damage = args[i + 3] instanceof Integer ? c(args[i + 3]) : 0;

				entities.put(entity, new BooleanSetting()
					.name((String) args[i + 1])
					.config(getConfigKey() + ".entities." + entity.getSimpleName())
					.icon(args[i + 2] instanceof Item ? new ItemStack((Item) args[i + 2], 1, damage) : new ItemStack((Block) args[i + 2], 1, damage))
					.defaultValue(entity == EntityPlayer.class));
				if (args[i + 3] instanceof Integer)
					i++;
			} catch (ClassCastException e) {
				System.err.println("Failed @ " + i);
				e.printStackTrace();
			}
		}
		List<SettingsElement> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(SettingsElement::getDisplayName));
		enabled.subSettings(settings);
	}

	@EventListener
	public void onDisplayNameRender(InvisibilityCheckEvent event) {
		Class<?> entity = event.entity.getClass();
		do {
			if (entities.containsKey(entity)) {
				event.setCanceled(entities.get(entity).get());
				break;
			}
			entity = entity.getSuperclass();
		} while (entity != null);
	}

	public static float getRenderModelAlpha() {
		if(!INSTANCE.isEnabled())
			return 0.15f;

		return 0.01f * (100f - INSTANCE.opacity.get());
	}

}
