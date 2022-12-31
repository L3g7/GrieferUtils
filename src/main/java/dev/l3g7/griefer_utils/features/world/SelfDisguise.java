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

package dev.l3g7.griefer_utils.features.world;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.event.events.render.InvisibilityCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST;

/**
 * Automatically sprints when walking.
 */
@Singleton
public class SelfDisguise extends Feature {

	private static final Map<String, String> RENAMED_ENTITIES = new HashMap<String, String>() {{
		put("minecart", "MinecartRideable");
		put("horse", "EntityHorse");
		put("iron_golem", "VillagerGolem");
		put("magma_cube", "LavaSlime");
		put("ocelot", "Ozelot");
		put("snowman", "SnowMan");
		put("falling_block", "FallingSand");
	}};

	public Entity currentDisguise = null;
	private boolean blockCoordinates = false;
	private String lastSentDisguiseCommand = null;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("SelfDisguise")
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
	public void onCityBuildJoin(CityBuildJoinEvent event) {
		resetDisguise();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerQUit(ServerQuitEvent event) {
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
	public void onReceive(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().equals("[GrieferGames] Verwandlungen sind auf diesem Grundstück deaktiviert. Deine aktuelle Verwandlung wurde aufgehoben.")) {
			resetDisguise();
			return;
		}

		if (lastSentDisguiseCommand == null)
			return;

		// Return if the entity doesn't exist
		if (event.message.getUnformattedText().startsWith("Falsche Benutzung: ")) {
			lastSentDisguiseCommand = null;
			return;
		}

		if (!event.message.getUnformattedText().startsWith("Du bist nun als "))
			return;

		if (currentDisguise != null)
			world().removeEntity(currentDisguise);

		loadDisguise(lastSentDisguiseCommand);
		lastSentDisguiseCommand = null;
	}

	@EventListener(priority = LOWEST)
	public void onDisplayNameRender(InvisibilityCheckEvent event) {
		if (event.entity == currentDisguise || (currentDisguise != null && event.entity == player())) {
			event.setCanceled(false);
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
		String[] arguments = command.split(" ");

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
			LabyMod.getInstance().displayMessageInChat(Constants.ADDON_PREFIX + "§cUnbekannte Verkleidung: " + arguments[1]);
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
				if (args.remove("block_coordinates", null)) {
					blockCoordinates = true;
				}
				if (args.containsKey("material")) {
					IBlockState block = Block.getBlockFromName(args.remove("material")).getDefaultState();
					Reflection.set(currentDisguise, block, "fallTile"); // , "field_175132_d", "d"
				}
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
			}

			if (!args.isEmpty())
				LabyMod.getInstance().displayMessageInChat(Constants.ADDON_PREFIX + "§cUnbekannte Argumente: " + args.keySet());
		}
	}

}
