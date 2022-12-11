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
		add(EntityArmorStand.class, "Armorstand", armor_stand, 0);
		add(EntityBat.class, "Fledermaus", spawn_egg, 65);
		add(EntityBlaze.class, "Blaze", spawn_egg, 161);
		add(EntityCaveSpider.class, "Höhlenspinne", spawn_egg, 59);
		add(EntityChicken.class, "Huhn", spawn_egg, 93);
		add(EntityCow.class, "Kuh", spawn_egg, 92);
		add(EntityCreeper.class, "Creeper", spawn_egg, 50);
		add(EntityDragon.class, "Enderdrache", dragon_egg, 0);
		add(EntityEnderman.class, "Enderman", spawn_egg, 58);
		add(EntityEndermite.class, "Endermite", spawn_egg, 67);
		add(EntityFallingBlock.class, "FallingBlock", sand, 0);
		add(EntityGhast.class, "Ghast", spawn_egg, 56);
		add(EntityGiantZombie.class, "Riese", spawn_egg, 54);
		add(EntityGuardian.class, "Guardian", spawn_egg, 68);
		add(EntityHorse.class, "Pferd", spawn_egg, 100);
		add(EntityIronGolem.class, "Eisengolem", iron_block, 0);
		add(EntityMagmaCube.class, "Magmawürfel", spawn_egg, 62);
		add(EntityMooshroom.class, "Pilzkuh", spawn_egg, 96);
		add(EntityOcelot.class, "Ozelot", spawn_egg, 98);
		add(EntityPig.class, "Schwein", spawn_egg, 90);
		add(EntityPigZombie.class, "Schweinezombie", spawn_egg, 57);
		add(EntityPlayer.class, "Spieler", skull, 3);
		add(EntityRabbit.class, "Hase", spawn_egg, 101);
		add(EntitySheep.class, "Schaf", spawn_egg, 91);
		add(EntitySilverfish.class, "Silberfischchen", spawn_egg, 60);
		add(EntitySkeleton.class, "Skelett", spawn_egg, 51);
		add(EntitySlime.class, "Slime", spawn_egg, 55);
		add(EntitySnowman.class, "Schneegolem", snowball, 0);
		add(EntitySpider.class, "Spinne", spawn_egg, 52);
		add(EntitySquid.class, "Tintenfisch", spawn_egg, 94);
		add(EntityVillager.class, "Dorfbewohner", spawn_egg, 120);
		add(EntityWitch.class, "Hexe", spawn_egg, 66);
		add(EntityWolf.class, "Wolf", spawn_egg, 95);
		add(EntityZombie.class, "Zombie", spawn_egg, 54);

		List<SettingsElement> settings = new ArrayList<>(entities.values());
		settings.sort(Comparator.comparing(SettingsElement::getDisplayName));
		enabled.subSettings(settings);
	}

	private void add(Class<? extends Entity> entity, String name, Object item, int damage) {
		entities.put(entity, new BooleanSetting()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon(item instanceof Item ? new ItemStack((Item) item, 1, damage) : new ItemStack((Block) item, 1, damage))
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
