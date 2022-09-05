package dev.l3g7.griefer_utils.features.tweaks.item_info;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.features.tweaks.item_info.ItemInfo.ItemInfoSupplier;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
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
		if (itemStack.getRepairCost() < 1)
			return 'a';

		return ItemUtil.canBeRepaired(itemStack) ? 'e' : 'c';
	}

}
