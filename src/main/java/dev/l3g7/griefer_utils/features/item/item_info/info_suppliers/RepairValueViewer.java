package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.features.item.item_info.ItemInfo;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.List;

@Singleton
public class RepairValueViewer extends ItemInfo.ItemInfoSupplier {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Reparaturwert anzeigen")
		.icon(Material.ANVIL);

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		int cost = itemStack.getRepairCost();

		return ImmutableList.of("§r", "§r§7Reparaturwert: §r§" + getColor(itemStack) + cost);
	}

	/**
	 *  0   - Green
	 * 1-39 - Yellow
	 * ≥ 40 - Red
	 */
	private char getColor(ItemStack itemStack) {
		if (itemStack.getRepairCost() < 1)
			return 'a';

		return ItemUtil.canBeRepaired(itemStack) ? 'e' : 'c';
	}

}
