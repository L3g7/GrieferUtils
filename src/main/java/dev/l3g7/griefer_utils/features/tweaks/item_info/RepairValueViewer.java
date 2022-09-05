package dev.l3g7.griefer_utils.features.tweaks.item_info;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.features.tweaks.item_info.ItemInfo.ItemInfoSupplier;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

@Singleton
public class RepairValueViewer extends ItemInfoSupplier {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Reparaturwert anzeigen")
			.icon(Material.ANVIL)
			.defaultValue(false)
			.config("tweaks.item_info.repair_value_viewer.active");

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		if (!isActive())
			return Collections.emptyList();

		int cost = itemStack.getRepairCost();

		return ImmutableList.of("§r", "§r§7Reparaturwert: §r§" + getColor(itemStack) + cost);
	}

	/**
	 *  0   - Green
	 * 1-39 - Yellow
	 * ≥ 40 - Red
	 */
	private char getColor(ItemStack itemStack) {
		// The repair cost of the item (see ContainerRepair.updateRepairOutput())
		// If the item is only damaged 1/4, you can repair it with a single material (i.e. a diamond), thus costing only 1 level
		// more than the repair value. Otherwise, it can be repaired with another item of the same type (i.e. a diamond sword), costing only 2 levels more.
		int cost = itemStack.getRepairCost() + (itemStack.getItemDamage() >= itemStack.getMaxDamage() / 4 ? 1 : 2);

		if(cost < 1)
			return 'a';
		if(cost > 39)
			return 'c';
		return 'e';
	}
}
