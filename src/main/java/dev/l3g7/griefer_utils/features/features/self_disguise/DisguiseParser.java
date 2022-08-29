package dev.l3g7.griefer_utils.features.features.self_disguise;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.main.LabyMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.features.Feature.player;
import static dev.l3g7.griefer_utils.features.Feature.world;

/**
 * @author L3g73 (L3g7 said i should put that there ._.)
 */
public class DisguiseParser {

	private static final Map<String, String> RENAMED_ENTITIES = new HashMap<String, String>() {{
		put("minecart", "MinecartRideable");
		put("horse", "EntityHorse");
		put("iron_golem", "VillagerGolem");
		put("magma_cube", "LavaSlime");
		put("ocelot", "Ozelot");
		put("snowman", "SnowMan");
		put("falling_block", "FallingSand");
	}};

	private static final Map<Class<? extends Entity>, Consumer<String>> ARGUMENT_HANDLERS = new HashMap<Class<? extends Entity>, Consumer<String>>(){{
		put(EntityArmorStand.class, DisguiseParser::armorStand);
		put(EntityCreeper.class, DisguiseParser::creeper);
		put(EntityFallingBlock.class, DisguiseParser::fallingBlock);
		put(EntityHorse.class, DisguiseParser::horse);
		put(EntityPig.class, DisguiseParser::pig);
		put(EntitySheep.class, DisguiseParser::sheep);
		put(EntitySlime.class, DisguiseParser::slime);
		put(EntityVillager.class, DisguiseParser::villager);
		put(EntityWolf.class, DisguiseParser::wolf);
	}};

	private static final List<String> VILLAGER_PROFESSIONS = ImmutableList.of("farmer", "librarian", "priest", "blacksmith", "butcher", "nitwit");

	private static Disguise disguise;
	
	public static Disguise parse(String command) {

		String[] arguments = command.split(" ");

		disguise = new Disguise(parseEntityType(arguments[1]));

		world().addEntityToWorld(-Math.abs(new Random().nextInt()), disguise.getEntity());

		if (arguments.length > 2) {
			try {
				parseArguments(arguments);
			} catch (IllegalArgumentException e) {
				LabyMod.getInstance().displayMessageInChat(Constants.ADDON_PREFIX + "§cUnbekanntes Argument: " + e.getMessage());
			}
		}

		return disguise;
	}

	private static void parseArguments(String[] arguments) {
		Consumer<String> argumentHandler = ARGUMENT_HANDLERS.get(disguise.getEntity().getClass());

		if(argumentHandler == null)
			return;

		for (int i = 2; i < arguments.length; i++)
			argumentHandler.accept(arguments[i]);
	}

	private static Entity parseEntityType(String entityName) {
		Entity entity = null;

		// Special entities
		switch (entityName) {
			case "elder_guardian":
				entity = new EntityGuardian(world());
				((EntityGuardian) entity).setElder();
				break;
			case "armor_stand":
				entity = new EntityArmorStand(world());
				for (int i = 0; i < 4; i++)
					entity.setCurrentItemOrArmor(i + 1, player().inventory.armorInventory[i]);
				break;
			case "falling_block":
				entity = new EntityFallingBlock(world(), player().posX, player().posY, player().posZ, Blocks.stone.getDefaultState());
				break;
			case "skeletal_horse":
			case "undead_horse":
				entity = new EntityHorse(world());
				((EntityHorse) entity).setHorseType(entityName.equals("undead_horse") ? 3 : 4);
				break;
		}

		if (entity != null)
			return entity;

		// Renamed entities
		if (RENAMED_ENTITIES.containsKey(entityName))
			return EntityList.createEntityByName(RENAMED_ENTITIES.get(entityName), world());

		// All other entities
		return EntityList.createEntityByName(toCamelCase(entityName), world());
	}

	private static String toCamelCase(String snakeCase) {
		char[] chars = snakeCase.toCharArray();
		List<Character> formattedChars = new ArrayList<>();

		formattedChars.add(Character.toUpperCase(chars[0]));

		for (int i = 1; i < chars.length; i++)
			if (chars[i] == '_')
				formattedChars.add(Character.toUpperCase(chars[++i]));
			else
				formattedChars.add(chars[i]);

		return formattedChars.stream().map(Objects::toString).collect(Collectors.joining());
	}

	private static boolean check(String expectedArgument, String argument) {
		if (expectedArgument.equals(argument))
			return true;

		throw new IllegalArgumentException(argument);
	}

	private static void armorStand(String argument) {
		if (check("show-arms", argument))
			disguise.getEntity().getDataWatcher().updateObject(10, (byte) (disguise.getEntity().getDataWatcher().getWatchableObjectByte(10) | 4));
	}

	private static void creeper(String argument) {
		if (check("powered", argument))
			disguise.getEntity().getDataWatcher().updateObject(17, (byte) 1);
	}

	private static void fallingBlock(String argument) {
		if (argument.equals("block_coordinates")) {
			disguise.enableBlockCoordinates();
			return;
		}

		else if (!argument.startsWith("material="))
			throw new IllegalArgumentException(argument);

		System.out.println(argument);
		System.out.println(argument.substring(9));
		IBlockState block = Block.getBlockFromName(argument.substring(9)).getDefaultState();
		Reflection.set(disguise.getEntity(), block, "fallTile", "field_175132_d", "d");
	}

	private static void horse(String argument) {
		if(check("saddled", argument))
			((EntityHorse) disguise.getEntity()).setHorseSaddled(true);
	}

	private static void pig(String argument) {
		if(check("saddled", argument))
			((EntityPig) disguise.getEntity()).setSaddled(true);
	}

	private static void sheep(String argument) {
		if (argument.equals("light-gray"))
			argument = "silver";

		for (EnumDyeColor value : EnumDyeColor.values())
			if (argument.equals(value.getName())) {
				((EntitySheep) disguise.getEntity()).setFleeceColor(value);
				return;
			}

		throw new IllegalArgumentException(argument);
	}

	private static void slime(String size) {
		ImmutableList<String> sizes = ImmutableList.of("tiny", "normal", "big");

		if (!sizes.contains(size))
			throw new IllegalArgumentException(size);

		Reflection.invoke(disguise.getEntity(), new String[]{"setSlimeSize", "func_70799_a", "a"}, Math.pow(2, sizes.indexOf(size)));
	}

	private static void villager(String argument) {
		if (VILLAGER_PROFESSIONS.contains(argument))
			((EntityVillager) disguise.getEntity()).setProfession(VILLAGER_PROFESSIONS.indexOf(argument));
		else
			throw new IllegalArgumentException(argument);
	}

	private static void wolf(String argument) {
		if (argument.equals("tames"))
			((EntityWolf) disguise.getEntity()).setTamed(true);
		else if (check("angry", argument))
			((EntityWolf) disguise.getEntity()).setAngry(true);
	}

}
