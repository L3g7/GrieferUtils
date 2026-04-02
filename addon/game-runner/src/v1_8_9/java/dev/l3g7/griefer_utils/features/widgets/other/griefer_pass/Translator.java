package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

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
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;

import java.util.*;

class Translator {

	public static final boolean lookupFailed;

	private static final Map<String, String> ITEM_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> BLOCK_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> ENTITY_TRANSLATION_KEYS = new HashMap<>();

	private static final Map<String, String> ENGLISH_ITEM_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> ENGLISH_BLOCK_TRANSLATION_KEYS = new HashMap<>();
	private static final Map<String, String> ENGLISH_ENTITY_TRANSLATION_KEYS = new HashMap<>();

	private static final Map<String, ItemStack> ITEMS = new HashMap<>();
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

		parseTranslationKeys(properties, ITEM_TRANSLATION_KEYS, BLOCK_TRANSLATION_KEYS, ENTITY_TRANSLATION_KEYS);

		StringTranslate fallbackTranslate = Reflection.get(StatCollector.class, "fallbackTranslator");
		Map<String, String> fallbackMap = Reflection.get(fallbackTranslate, "languageList");
		parseTranslationKeys(fallbackMap, ENGLISH_ITEM_TRANSLATION_KEYS, ENGLISH_BLOCK_TRANSLATION_KEYS, ENGLISH_ENTITY_TRANSLATION_KEYS);

		for (Block block : Block.blockRegistry) {
			Item item = Item.getItemFromBlock(block);
			if (item == null)
				continue;

			List<ItemStack> itemStacks = new ArrayList<>();
			block.getSubBlocks(item, CreativeTabs.tabAllSearch, itemStacks);
			for (ItemStack itemStack : itemStacks)
				BLOCKS.put(itemStack.getUnlocalizedName(), new Pair<>(block, itemStack.getMetadata()));
		}

		for (Item item : Item.itemRegistry) {
			List<ItemStack> itemStacks = new ArrayList<>();
			item.getSubItems(item, CreativeTabs.tabAllSearch, itemStacks);
			for (ItemStack itemStack : itemStacks)
				ITEMS.put(itemStack.getUnlocalizedName(), itemStack);
		}

		ENTITIES = Reflection.get(EntityList.class, "stringToClassMapping");

		// Hardcoded renamed elements
		addAlias(ENTITY_TRANSLATION_KEYS, "magmawürfel", "magmaschleim");
		addAlias(ITEM_TRANSLATION_KEYS, "kabeljau", "roher kabeljau");
		addAlias(ITEM_TRANSLATION_KEYS, "lachs", "roher lachs");
	}

	private static void parseTranslationKeys(Map<String, String> properties, Map<String, String> items,	Map<String, String> blocks,	Map<String, String> entities) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().toLowerCase();
			if (key.startsWith("item."))
				items.put(value, key);
			else if (key.startsWith("tile."))
				blocks.put(value, key);
			else if (key.startsWith("entity."))
				entities.put(value, key);
		}
	}

	private static void addAlias(Map<String, String> map, String key, String replacement) {
		ENTITY_TRANSLATION_KEYS.put(key, ENTITY_TRANSLATION_KEYS.get(replacement));
	}

	public static ItemStack getItem(String germanName) {
		return resolve("Item", germanName.toLowerCase(), ITEM_TRANSLATION_KEYS, ENGLISH_ITEM_TRANSLATION_KEYS, ITEMS);
	}

	public static Pair<Block, Integer> getBlock(String germanName) {
		return resolve("Block", germanName.toLowerCase(), BLOCK_TRANSLATION_KEYS, ENGLISH_BLOCK_TRANSLATION_KEYS, BLOCKS);
	}

	public static Class<? extends Entity> getEntity(String germanName) {
		return resolve("Entity", germanName.toLowerCase(), ENTITY_TRANSLATION_KEYS, ENGLISH_ENTITY_TRANSLATION_KEYS, ENTITIES);
	}

	private static <T> T resolve(String type, String germanName, Map<String, String> translationKeys, Map<String, String> englishTranslationKeys, Map<String, T> lookup) {
		String translationKey = translationKeys.get(germanName);
		if (translationKey == null) {
			translationKey = englishTranslationKeys.get(germanName);
			if (translationKey == null)
				return reportError(type + " GName -> TKey", germanName);
		}

		translationKey = translationKey.substring(0, translationKey.length() - ".name".length());
		if (translationKeys == ENTITY_TRANSLATION_KEYS) {
			translationKey = translationKey.substring("entity.".length());
		}

		T t = lookup.get(translationKey);
		if (t == null) {
			t = lookup.get(translationKey + ".default");
			if (t == null)
				t = lookup.get(translationKey + ".normal");
			if (t == null)
				return reportError("TKey -> " + type, translationKey);
		}

		return t;
	}

	private static <T> T reportError(String op, String key) {
		if (!lookupFailed)
			BugReporter.reportError(new Throwable(op + " failed for " + key));
		return null;
	}

}
