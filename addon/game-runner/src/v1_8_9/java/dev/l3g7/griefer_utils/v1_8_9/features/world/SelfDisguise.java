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

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.api.BugReporter;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static dev.l3g7.griefer_utils.api.event.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

/**
 * Automatically sprints when walking.
 */
@Singleton
public class SelfDisguise extends Feature {

	private static final Pattern LUCKY_SWORD_DISGUISE_PATTERN = Pattern.compile("^§r§8\\[§r§e§lLuckySword§r§8] " + Constants.FORMATTED_PLAYER_PATTERN.pattern() + " §r§7ist nun als §r§e(?<disguise>\\w+) §r§7verkleidet!§r$");
	private static final Map<String, String> RENAMED_ENTITIES = new HashMap<String, String>() {{
		put("minecart", "MinecartRideable");
		put("horse", "EntityHorse");
		put("iron_golem", "VillagerGolem");
		put("magma_cube", "LavaSlime");
		put("cat", "Ozelot");
		put("ocelot", "Ozelot");
		put("snowman", "SnowMan");
		put("falling_block", "FallingSand");
	}};

	public Entity currentDisguise = null;
	private boolean blockCoordinates = false;
	private String lastSentDisguiseCommand = null;
	private String[] unknownArgs = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verkleidung in 3rd Person")
		.description("Erlaubt das Sehen der derzeitigen Verkleidung im Third-Person-Modus.")
		.icon("steve_creeper")
		.callback(v -> { if (!v) hideDisguise(); });

	public void init() {
		super.init();
		getCategory().getSetting()
			.callback(v -> { if (!v) hideDisguise(); });
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
		if (event.message.equals("/ud")) {
			resetDisguise();
		} else if (event.message.startsWith("/d "))
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
				BugReporter.reportError(new Throwable("Sword disguise \"" + entity + "\" is now known"));
				return;
			}
			world().addEntityToWorld(currentDisguise.getEntityId(), currentDisguise);
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

		TickScheduler.runAfterClientTicks(() -> {
			try {
				loadDisguise(lastSentDisguiseCommand);
			} catch (Throwable t) {
				throw new RuntimeException("Error when disguising with command: \"" + lastSentDisguiseCommand + "\" " + t.getClass().getSimpleName()  + " : " + t.getMessage());
			}
			unknownArgs = null;
			lastSentDisguiseCommand = null;
		}, 1);
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
		currentDisguise = null;
		blockCoordinates = false;
		switch (arguments[1]) {
			case "elder_guardian":
				currentDisguise = new EntityGuardian(world());
				((EntityGuardian) currentDisguise).setElder();
				break;
			case "armor_stand":
				currentDisguise = new EntityArmorStand(world());
				for (int i = 0; i < 4; i++)
					currentDisguise.setCurrentItemOrArmor(i + 1, player().inventory.armorInventory[i]);
				break;
			case "block":
			case "falling_block":
				currentDisguise = new EntityFallingBlock(world(), player().posX, player().posY, player().posZ, Blocks.stone.getDefaultState());
				break;
			case "skeletal_horse":
			case "undead_horse":
				currentDisguise = new EntityHorse(world());
				((EntityHorse) currentDisguise).setHorseType(arguments[1].equals("undead_horse") ? 3 : 4);
				break;
			case "bat":
				currentDisguise = new EntityBat(world());
				((EntityBat) currentDisguise).setIsBatHanging(false);
		}

		if (currentDisguise == null) {
			// Renamed entities
			if (RENAMED_ENTITIES.containsKey(arguments[1]))
				currentDisguise = EntityList.createEntityByName(RENAMED_ENTITIES.get(arguments[1]), world());

			// All other entities
			else
				currentDisguise = EntityList.createEntityByName(LOWER_UNDERSCORE.to(UPPER_CAMEL, arguments[1]), world());
		}

		if (currentDisguise == null) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§cUnbekannte Verkleidung: " + arguments[1]);
			return;
		}

		world().addEntityToWorld(currentDisguise.getEntityId(), currentDisguise);

		if (arguments.length > 2) {
			Map<String, String> args = new HashMap<>();
			for (int i = 2; i < arguments.length; i++) {
				String[] parts = arguments[i].split("=");
				args.put(parts[0], parts.length == 1 ? null : parts[1]);
			}

			if (currentDisguise instanceof EntityArmorStand) {
				if (args.remove("show-arms", null))
					currentDisguise.getDataWatcher().updateObject(10, (byte) (currentDisguise.getDataWatcher().getWatchableObjectByte(10) | 4));
			} else if (currentDisguise instanceof EntityCreeper) {
				if (args.remove("powered", null))
					currentDisguise.getDataWatcher().updateObject(17, (byte) 1);
			} else if (currentDisguise instanceof EntityFallingBlock) {
				if (args.remove("block_coordinates", null))
					blockCoordinates = true;

				String material = args.remove("material");
				if (material == null)
					return;

				Block block = Block.getBlockFromName(material);
				if (block == null)
					return;

				IBlockState blockState = Block.getBlockFromName(material).getDefaultState();
				Reflection.set(currentDisguise, "fallTile", blockState);
			} else if (currentDisguise instanceof EntityHorse) {
				if (args.remove("saddled", null))
					((EntityHorse) currentDisguise).setHorseSaddled(true);
			} else if (currentDisguise instanceof EntityPig) {
				if (args.remove("saddled", null))
					((EntityPig) currentDisguise).setSaddled(true);
			} else if (currentDisguise instanceof EntitySheep) {
				if (args.remove("light-gray", null))
					((EntitySheep) currentDisguise).setFleeceColor(EnumDyeColor.SILVER);

				for (EnumDyeColor value : EnumDyeColor.values())
					if (args.remove(value.getName(), null))
						((EntitySheep) currentDisguise).setFleeceColor(value);
			} else if (currentDisguise instanceof EntitySlime) {
				int size = 0;
				if (args.remove("tiny", null))
					size = 1;
				else if (args.remove("normal", null))
					size = 2;
				else if (args.remove("big", null))
					size = 4;

				Reflection.invoke(currentDisguise, "setSlimeSize", Math.pow(2, size));
			} else if (currentDisguise instanceof EntityVillager) {
				int index = 0;
				for (String profession : new String[]{"farmer", "librarian", "priest", "blacksmith", "butcher", "nitwit"}) {
					if (args.remove(profession, null))
						((EntityVillager) currentDisguise).setProfession(index);
					index++;
				}
			} else if (currentDisguise instanceof EntityWolf) {
				if (args.remove("tamed", null))
					((EntityWolf) currentDisguise).setTamed(true);
				else if (args.remove("angry", null))
					((EntityWolf) currentDisguise).setAngry(true);
			} else if (currentDisguise instanceof EntityEnderman) {
				String blockType = args.remove("block");
				if (blockType == null)
					return;

				Block block = Block.getBlockFromName(blockType);
				if (block == null)
					return;

				IBlockState blockState = Block.getBlockFromName(blockType).getDefaultState();
				((EntityEnderman) currentDisguise).setHeldBlockState(blockState);
			}

			if (!args.isEmpty())
				LabyBridge.display(Constants.ADDON_PREFIX + "§cUnbekannte Argumente: " + args.keySet());
		}
	}

}
