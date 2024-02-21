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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SliderSetting;
import net.labymod.settings.elements.SettingsElement;
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
import static dev.l3g7.griefer_utils.features.render.TrueSight.ToggleMode.*;

/**
 * Shows invisible entities.
 */
@Singleton
public class TrueSight extends Feature {

	private static final TrueSight INSTANCE = FileProvider.getSingleton(TrueSight.class);
	private static final Map<Class<?>, String> CLASS_TO_STRING_MAPPING = Reflection.get(EntityList.class, "classToStringMapping");

	private final Map<Class<? extends Entity>, BooleanSetting> entities = new HashMap<>();

	private final SliderSetting opacity = new SliderSetting()
		.name("Durchsichtigkeit (%)")
		.description("Wie durchsichtig ein eigentlich unsichtbares Entity sein soll.")
		.icon("fading_steve")
		.min(0).max(100)
		.defaultValue(85);

	private boolean togglingAll; // NOTE refactor
	private final DropDownSetting<ToggleMode> toggleAll = new DropDownSetting<>(ToggleMode.class, 1)
		.name("Alle umschalten")
		.icon("scroll")
		.defaultValue(ALL_OFF)
		.callback(v -> {
			if (v == CUSTOM)
				return;

			togglingAll = true;
			for (BooleanSetting setting : entities.values())
				setting.set(v == ALL_ON);
			togglingAll = false;
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Unsichtbare Entities anzeigen")
		.description("Macht unsichtbare Entities sichtbar.")
		.icon("blue_light_bulb")
		.subSettings(opacity);

	@Override
	public void init() {
		super.init();
		enabled.subSettings(Arrays.asList(toggleAll, new HeaderSetting())); // hotfix to prevent toggleAll from being saved
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
			.name("Block")
			.config(getConfigKey() + ".entities.falling_block")
			.icon("stone")
			.callback(this::updateGlobalToggle));
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
		entities.put(EntityPlayer.class, new BooleanSetting()
			.name("Spieler")
			.config(getConfigKey() + ".entities.spieler")
			.icon("steve")
			.callback(this::updateGlobalToggle));
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

		updateGlobalToggle();
	}

	private void add(Class<? extends Entity> entity, String name) {
		entities.put(entity, new BooleanSetting()
			.name(name)
			.config(getConfigKey() + ".entities." + UPPER_CAMEL.to(LOWER_UNDERSCORE, name))
			.icon("mob_icons/faithless/" + CLASS_TO_STRING_MAPPING.get(entity).toLowerCase())
			.defaultValue(entity == EntityPlayer.class)
			.callback(this::updateGlobalToggle));
	}

	private void updateGlobalToggle() {
		if(togglingAll)
			return;

		boolean enabled = entities.values().iterator().next().get();
		boolean allEqual = entities.values().stream().allMatch(s -> s.get() == enabled);

		toggleAll.set(!allEqual ? CUSTOM : enabled ? ALL_ON : ALL_OFF);
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

	public enum ToggleMode implements Named {
		CUSTOM(""), ALL_ON("Alle an"), ALL_OFF("Alle aus");

		private final String name;

		ToggleMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
