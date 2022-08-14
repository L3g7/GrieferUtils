package dev.l3g7.griefer_utils.features.tweaks.item_info.grieferwert;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GWEntry {

    private final Item resolvedItem;
    private final int damage;

    private final String name;
    private final String price;

    public GWEntry(JsonObject object) {
        JsonObject mapping = object.get("mapping").getAsJsonObject();
        this.resolvedItem = Item.getByNameOrId(mapping.get("id").getAsString());
        this.damage = mapping.get("damage").getAsInt();

        JsonObject wertItem = object.get("wertItem").getAsJsonObject();
        this.name = wertItem.get("name").getAsString().replaceAll("  +", " "); // Fixing what mysterymod couldn't
        this.price = (wertItem.get("priceRange").getAsString().replace("-", "$ - ") + "$").replaceAll("\\.0+(?!\\d)", "");
    }

    public boolean testItem(ItemStack stack) {
        return stack.getItem() == resolvedItem && stack.getMaxDamage() == damage;
    }

    public String toTooltipString() {
        return "§r§f" + name + "§r§7: §e" + price;
    }

}
