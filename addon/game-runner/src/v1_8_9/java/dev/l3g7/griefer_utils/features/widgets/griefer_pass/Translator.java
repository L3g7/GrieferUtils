package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Locale;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

class Translator {

	public static final boolean lookupFailed;

	private static final Map<String, String> ITEM_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> BLOCK_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> ENTITIY_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, Item> ITEMS = new HashMap<>();
	private static final Map<String, Pair<Block, Integer>> BLOCKS = new HashMap<>();
	private static final Map<String, Class<? extends Entity>> ENTITIES;

	static {
		Locale locale = new Locale();
		locale.loadLocaleDataFiles(Minecraft.getMinecraft().getResourceManager(), Arrays.asList("de_DE"));
		Map<String, String> properties = Reflection.get(locale, "properties");
		lookupFailed = properties.isEmpty();
		if (lookupFailed) {
			// If this happens in the dev env: Just use a resource pack lol
			BugReporter.reportError(new Throwable("Lookup failed! (de_DE not found)"));
		}

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith("item."))
				ITEM_TRANSLATION_KEYS.put(entry.getValue(), key);
			else if (key.startsWith("tile."))
				BLOCK_TRANSLATION_KEYS.put(entry.getValue(), key);
			else if (key.startsWith("entity."))
				ENTITIY_TRANSLATION_KEYS.put(entry.getValue(), key);
		}

		for (Block block : Block.blockRegistry) {
			Item item = Item.getItemFromBlock(block);
			if (item == null)
				continue;

			List<ItemStack> itemStacks = new ArrayList<>();
			block.getSubBlocks(item, CreativeTabs.tabAllSearch, itemStacks);
			for (ItemStack itemStack : itemStacks)
				BLOCKS.put(itemStack.getUnlocalizedName(), new Pair<>(block, itemStack.getMetadata()));
		}

		for (Item item : Item.itemRegistry)
			ITEMS.put(item.getUnlocalizedName(), item);

		ENTITIES = Reflection.get(EntityList.class, "stringToClassMapping");
	}

	public static Item getItem(String germanName) {
		return resolve("Item", germanName, ITEM_TRANSLATION_KEYS, ITEMS);
	}

	public static Pair<Block, Integer> getBlock(String germanName) {
		return resolve("Block", germanName, BLOCK_TRANSLATION_KEYS, BLOCKS);
	}

	public static Class<? extends Entity> getEntity(String germanName) {
		return resolve("Entity", germanName, ENTITIY_TRANSLATION_KEYS, ENTITIES);
	}

	private static <T> T resolve(String type, String germanName, Map<String, String> translationKeys, Map<String, T> lookup) {
		String translationKey = translationKeys.get(germanName);
		if (translationKey == null)
			return reportError(type + " GName -> TKey", germanName);

		translationKey = translationKey.substring(0, translationKey.length() - ".name".length());
		if (translationKeys == ENTITIY_TRANSLATION_KEYS)
			translationKey = translationKey.substring("entity.".length());

		T t = lookup.get(translationKey);
		if (t == null)
			return reportError("TKey -> " + type, translationKey);

		return t;
	}

	private static <T> T reportError(String op, String key) {
		if (!lookupFailed)
			BugReporter.reportError(new Throwable(op + " failed for " + key));
		return null;
	}

}
