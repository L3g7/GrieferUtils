package dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.data;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.ItemUtil.ItemEnchantment;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * An implementation of an Item-API parser for {@link dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.ByteAndBit}
 */
public class BABItem implements Comparable<BABItem> {

	private static final DecimalFormat priceFormat = new DecimalFormat("###,###,###.##");
	private final ItemStack stack;
	final float price;
	public AtomicInteger warehouseCount;

	public Availability getAvailability() {
		int count = warehouseCount.get();
		return count > 0 ? (count > 7 * stack.stackSize ? Availability.AVAILABLE : Availability.SCARCE) : Availability.EMPTY;
	}

	public ItemStack getStack() {
		ItemStack stack = this.stack.copy();
		var lore = ItemUtil.getLore(stack);
		lore.add("§r");
		lore.add("§r" + String.format(getAvailability().name, (warehouseCount.get() / stack.stackSize)));
		ItemUtil.setLore(stack, lore);
		return stack;
	}

	public float getPrice() {
		return price;
	}

	public BABItem(float price, ItemStack stack, AtomicInteger warehouseCount) {
		this.price = price;
		this.stack = stack;
		this.warehouseCount = warehouseCount;
	}

	@Override
	public int compareTo(BABItem o) {
		if (warehouseCount.get() == 0) return 1;
		if (o.warehouseCount.get() == 0) return -1;
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

	private static class VeloItem {
		VeloMaterial material = new VeloMaterial();
		private static final String defaultDisplayName = "§r";
		String displayName = defaultDisplayName;
		int warehouseCount = Integer.MAX_VALUE;
		int repairCost = 0;
		List<VeloLore> lore = Collections.emptyList();
		List<VeloEnchantment> enchantments = Collections.emptyList();
		List<VeloPrice> prices = Collections.emptyList();

		public List<BABItem> convert() {
			AtomicInteger i = new AtomicInteger(warehouseCount);
			if (displayName == null) displayName = defaultDisplayName;
			if (prices.isEmpty())
				return Collections.emptyList();

			List<BABItem> items = new ArrayList<>();
			ItemEnchantment[] itemEnchantments = enchantments.stream().map((e) -> new ItemEnchantment(e.id, e.level)).toArray(ItemEnchantment[]::new);
			for (var price : this.prices) {
				if (price.price == null) continue;
				if (price.price <= 0) continue; // ankauf - TODO

				ItemStack stack = new ItemStack(Blocks.stone, price.amount, 10000);
				if (material.name != null) {
					Item item = Item.getByNameOrId(material.name);
					if (item != null)
						stack = new ItemStack(item, price.amount, this.material.subID);
				}

				stack.setRepairCost(repairCost);
				stack.setStackDisplayName("§r" + displayName + " §r§a(" + priceFormat.format(price.price) + "$)");

				if (!lore.isEmpty())
					ItemUtil.setLore(stack, lore.stream().map(l -> l.lineContent).toArray(String[]::new));

				if (!enchantments.isEmpty()) ItemUtil.setEnchantments(stack, itemEnchantments);

				items.add(new BABItem(price.price, stack, i));
			}
			return items;
		}
	}

	private static class VeloMaterial {
		String name = "§r";
		int subID = 0;
		int stackSize = 1;
	}

	private static class VeloLore {
		String lineContent = "§r";
	}

	private static class VeloEnchantment {
		int id = 0;
		int level = 0;
	}

	private static class VeloPrice {
		Float price;
		int amount = 1;
	}

	public static List<BABItem> parse(JsonArray in) {
		ArrayList<VeloItem> items = IOUtil.gson.fromJson(in, new TypeToken<ArrayList<VeloItem>>() {}.getType());
		return items.stream().parallel().map(VeloItem::convert).flatMap(List::stream).collect(Collectors.toList());
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

	public enum Availability {

		AVAILABLE("§aVerfügbar"), SCARCE("§eKnapp (%d)"), EMPTY("§cAusverkauft");

		public final String name;

		Availability(String name) {
			this.name = name;
		}

	}

}