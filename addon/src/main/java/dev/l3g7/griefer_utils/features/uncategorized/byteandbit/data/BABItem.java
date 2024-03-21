package dev.l3g7.griefer_utils.features.uncategorized.byteandbit.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * description missing.
 */
public class BABItem implements Comparable<BABItem> {
	private static final DecimalFormat priceFormat = new DecimalFormat("###,###,###.#");
	private final ItemStack stack;
	final float price;

	public ItemStack getStack() {
		return stack;
	}

	public float getPrice() {
		return price;
	}

	public BABItem(float price, ItemStack stack) {
		this.price = price;
		this.stack = stack;
	}

	@Override
	public int compareTo(BABItem o) {

		Item otherItem = o.getStack().getItem();
		Item thisItem = this.getStack().getItem();

		int id = Item.getIdFromItem(otherItem);
		int tid = Item.getIdFromItem(thisItem);

		if (tid > id) return 1;
		if (tid < id) return -1;

		int otherSubID = o.getStack().getItemDamage();
		int thisSubID = this.getStack().getItemDamage();

		if (thisSubID > otherSubID) return 1;
		if (thisSubID < otherSubID) return -1;

		int otherAmount = o.getStack().stackSize;
		int thisAmount = this.getStack().stackSize;

		if (thisAmount > otherAmount) return 1;
		if (thisAmount < otherAmount) return -1;

		return Float.compare(this.price, o.getPrice());
	}


	public static List<BABItem> parse(JsonArray in) {
		ArrayList<BABItem> ret = new ArrayList<>();
		for (JsonElement itemElement : in) {
			JsonObject item = itemElement.getAsJsonObject();
			JsonObject material = item.get("material").getAsJsonObject();

			String materialName = material.get("name").getAsString();
			int subID = material.get("subID").getAsInt();
			int stackSize = material.get("stackSize").getAsShort(); // currently unused. Maybe useful in the future
			String displayName = item.get("displayName").getAsString();
			int repairCost = item.get("repairCost").getAsInt();
			JsonArray lore = item.get("lore").getAsJsonArray();
			String[] loreLines = new String[lore.size()];

			for (JsonElement loreLineElement : lore) {
				JsonObject loreLine = loreLineElement.getAsJsonObject();
				loreLines[loreLine.get("lineNumber").getAsInt()] = loreLine.get("lineContent").getAsString();
			}

			JsonArray ench = item.get("enchantments").getAsJsonArray();

			ArrayList<ItemUtil.ItemEnchantment> enchantments = new ArrayList<>();
			for (JsonElement enchElement : ench) {
				JsonObject enchObject = enchElement.getAsJsonObject();
				enchantments.add(new ItemUtil.ItemEnchantment(enchObject.get("id").getAsInt(), enchObject.get("level").getAsInt()));
			}
			for (JsonElement priceJson : itemElement.getAsJsonObject().get("prices").getAsJsonArray()) {
				// initialize a separate BABItem entry for each price/amount variation
				float price = priceJson.getAsJsonObject().get("price").getAsFloat();
				int amount = priceJson.getAsJsonObject().get("amount").getAsInt();

				ItemStack stack = new ItemStack(Item.getByNameOrId(materialName), amount, subID);
				stack.setRepairCost(repairCost);
				stack.setStackDisplayName(displayName + " §r§a(" + priceFormat.format(price) + ")$");

				if (loreLines.length > 0) ItemUtil.setLore(stack, loreLines);
				if (!enchantments.isEmpty()) ItemUtil.setEnchantments(stack, enchantments);

				ret.add(new BABItem(price, stack));
			}
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return Objects.hash(stack.hashCode(), price);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BABItem) return ItemStack.areItemStacksEqual(stack, ((BABItem) obj).stack);
		return false;
	}
}
