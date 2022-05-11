package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class RepairValueViewer extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Reparaturwert anzeigen")
			.icon(Material.ANVIL)
			.defaultValue(false)
			.config("features.repair_value_viewer.active");

	public RepairValueViewer() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent e) {
		if (!isActive())
			return;

		e.toolTip.add("§r");
		int cost = e.itemStack.getRepairCost();
		e.toolTip.add("§r§7Reparaturwert: §r§" + getColor(cost) + cost);
	}

	/**
	 *  0   - Green
	 * 1-34 - Yellow
	 * ≥ 35 - Red
	 */
	private char getColor(int cost) {
		if(cost < 1)
			return 'a';
		if(cost > 35)
			return 'c';
		return 'e';
	}
}
