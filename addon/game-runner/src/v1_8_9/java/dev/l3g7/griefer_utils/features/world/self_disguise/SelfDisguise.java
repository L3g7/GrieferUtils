/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.self_disguise;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.core.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.self_disguise.disguises.*;
import joptsimple.internal.Strings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Shows a preview for disguises in third person view.
 */
@Singleton
public class SelfDisguise extends Feature { // NOTE cleanup

	private static final Pattern LUCKY_SWORD_DISGUISE_PATTERN = Pattern.compile("^§r§8\\[§r§e§lLuckySword§r§8] " + Constants.FORMATTED_PLAYER_PATTERN.pattern() + " §r§7ist nun als §r§e(?<disguise>\\w+) §r§7verkleidet!§r$");

	private static final Map<String, Disguise<?>> DISGUISES = new HashMap<>() {{
		put("bat", new BatDisguise());
		put("blaze", new Disguise<>(EntityBlaze.class));
		put("cave_spider", new Disguise<>(EntityCaveSpider.class));
		put("chicken", new Disguise<>(EntityChicken.class));
		put("cow", new Disguise<>(EntityCow.class));
		put("creeper", new CreeperDisguise());
		put("elder_guardian", new ElderGuardianDisguise());
		put("enderman", new Disguise<>(EntityEnderman.class));
		put("endermite", new Disguise<>(EntityEndermite.class));
		put("guardian", new Disguise<>(EntityGuardian.class));
		put("horse", new HorseDisguise());
		put("iron_golem", new Disguise<>(EntityIronGolem.class));
		put("magma_cube", new SlimeDisguise<>(EntityMagmaCube.class));
		put("ocelot", new Disguise<>(EntityOcelot.class));
		put("cat", new Disguise<>(EntityOcelot.class));
		put("pig", new PigDisguise());
		put("rabbit", new Disguise<>(EntityRabbit.class));
		put("sheep", new SheepDisguise());
		put("silverfish", new Disguise<>(EntitySilverfish.class));
		put("skeletal_horse", new AbstractHorseDisguise(4));
		put("skeleton", new Disguise<>(EntitySkeleton.class));
		put("slime", new SlimeDisguise<>(EntitySlime.class));
		put("snowman", new Disguise<>(EntitySnowman.class));
		put("squid", new Disguise<>(EntitySquid.class));
		put("undead_horse",  new AbstractHorseDisguise(3));
		put("villager", new VillagerDisguise());
		put("witch", new Disguise<>(EntityWitch.class));
		put("wolf", new WolfDisguise());
		put("armor_stand", new ArmorStandDisguise());
		put("boat", new Disguise<>(EntityBoat.class));
		put("ender_crystal", new Disguise<>(EntityEnderCrystal.class));
		put("minecart", new Disguise<>(EntityMinecartEmpty.class));
		put("falling_block", new FallingBlockDisguise());
		put("block", new FallingBlockDisguise());
	}};

	public Entity currentDisguise = null;
	private boolean blockCoordinates = false;
	private String lastSentDisguiseCommand = null;
	private String[] unknownArgs = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("§yVerkleidung in 3rd Person")
		.description("Erlaubt das Sehen der derzeitigen Verkleidung im Third-Person-Modus.")
		.icon("mob_icons/faithless/creeper")
		.callback(v -> { if (!v) hideDisguise(); });

	public void init() {
		super.init();
		getCategory().callback(v -> { if (!v) hideDisguise(); });
	}

	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		Entity currentDisguise = this.currentDisguise;
		if (currentDisguise == null)
			return;

		// When you teleport somewhere far away, the entity is unloaded
		if (!world().loadedEntityList.contains(currentDisguise))
			world().loadEntities(ImmutableList.of(currentDisguise));

		if (mc().gameSettings.thirdPersonView == 0) {
			hideDisguise();
			return;
		}

		EntityPlayerSP p = player();
		player().setInvisible(true);
		currentDisguise.setInvisible(false);
		if (blockCoordinates)
			currentDisguise.setLocationAndAngles(((int) renderPos().x) + 0.5 * Math.signum(renderPos().x), (int) (renderPos().y + 0.5), ((int) renderPos().z) + 0.5 * Math.signum(renderPos().z), p.rotationYaw, p.rotationPitch);
		else
			currentDisguise.setLocationAndAngles(renderPos().x, renderPos().y, renderPos().z, p.rotationYaw, p.rotationPitch);
		currentDisguise.setRotationYawHead(p.getRotationYawHead());
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCitybuildJoin(CitybuildJoinEvent event) {
		resetDisguise();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerQuit(ServerQuitEvent event) {
		resetDisguise();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onSend(MessageSendEvent event) {
		if (event.message.equalsIgnoreCase("/ud") || event.message.equalsIgnoreCase("/undisguise")) {
			resetDisguise();
		} else if (event.message.toLowerCase().startsWith("/d ") || event.message.toLowerCase().startsWith("/disguise "))
			lastSentDisguiseCommand = event.message;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onReceive(MessageReceiveEvent event) {
		String text = event.message.getUnformattedText();
		if (text.equals("[GrieferGames] Verwandlungen sind auf diesem Grundstück deaktiviert. Deine aktuelle Verwandlung wurde aufgehoben.")) {
			resetDisguise();
			return;
		}

		Matcher swordMatcher = LUCKY_SWORD_DISGUISE_PATTERN.matcher(event.message.getFormattedText());
		if (swordMatcher.matches()) {
			resetDisguise();
			String name = swordMatcher.group("name").replaceAll("§.", "");
			if (!MinecraftUtil.name().equals(name))
				return;

			String entity = swordMatcher.group("disguise");
			if (entity.equals("Schaf")) {
				currentDisguise = new EntitySheep(world());
			} else if (entity.equals("Zombie")) {
				currentDisguise = new EntityZombie(world());
			} else {
				BugReporter.reportError(new Throwable("Sword disguise \"" + entity + "\" is not known"));
				return;
			}
			world().addEntityToWorld(currentDisguise.getEntityId(), currentDisguise);
			if (!isEnabled())
				hideDisguise();
			return;
		}

		if (lastSentDisguiseCommand == null)
			return;

		// Return if the entity doesn't exist
		if (text.startsWith("Falsche Benutzung: ") && text.endsWith(" sind unbekannte Argumente.")) {
			String args = text.substring("Falsche Benutzung: ".length(), text.length() - " sind unbekannte Argumente.".length());
			unknownArgs = args.replace('-', '_').split(", ");
			return;
		}

		if (!text.startsWith("Du bist nun als "))
			return;

		unknownArgs = null;
		if (currentDisguise != null)
			world().removeEntity(currentDisguise);

		TickScheduler.runNextClientTick(() -> {
			try {
				loadDisguise(lastSentDisguiseCommand);
			} catch (Throwable t) {
				throw new RuntimeException("Error when disguising with command: \"" + lastSentDisguiseCommand + "\" " + t.getClass().getSimpleName()  + " : " + t.getMessage());
			}
			unknownArgs = null;
			lastSentDisguiseCommand = null;
		});
	}

	@EventListener(priority = LOWEST)
	public void onDisplayNameRender(InvisibilityCheckEvent event) {
		if (event.entity == currentDisguise || (currentDisguise != null && event.entity == player())) {
			event.invisible = false;
		}
	}

	private void hideDisguise() {
		if (player() != null)
			player().setInvisible(false);
		if (currentDisguise != null) {
			currentDisguise.setInvisible(true);
			currentDisguise.setPosition(renderPos().x, -5, renderPos().z);
		}
	}

	private void resetDisguise() {
		if (player() != null)
			player().setInvisible(false);

		if (currentDisguise != null && world() != null)
			world().removeEntity(currentDisguise);

		currentDisguise = null;
		lastSentDisguiseCommand = null;
	}

	private void loadDisguise(String command) {
		command = command.replace('-', '_');
		List<String> commandArgs = new ArrayList<>(Arrays.asList(command.split(" ")));
		if (unknownArgs != null)
			commandArgs.removeAll(Arrays.asList(unknownArgs));
		String[] arguments = commandArgs.toArray(new String[0]);

		// Special entities
		Disguise<?> disguise = DISGUISES.get(arguments[1].toLowerCase());
		if (disguise == null) {
			if (isEnabled())
				LabyBridge.display(Constants.ADDON_PREFIX + "§cUnbekannte Verkleidung: " + arguments[1]);

			return;
		}

		String[] subArgs = new String[arguments.length - 2];
		System.arraycopy(arguments, 2, subArgs, 0, subArgs.length);
		Disguise.Arguments args = new Disguise.Arguments(subArgs);
		currentDisguise = disguise.create(args);
		blockCoordinates = disguise.clampCoordinates(args);

		if (!args.isEmpty() && isEnabled()) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§cUnbekannte Argumente: " + Strings.join(args, ", "));
			System.out.println(Arrays.toString(arguments));
			System.out.println(Arrays.toString(subArgs));
		}

		world().addEntityToWorld(currentDisguise.getEntityId(), currentDisguise);
		if (!isEnabled())
			hideDisguise();
	}

}
