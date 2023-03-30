package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.init.Items;

@Singleton
public class ShowHatItems extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Items auf dem Kopf anzeigen")
		.description("Zeigt Items, die Spieler im Kopf-Slot haben, über ihnen an.")
		.icon(ItemUtil.createItem(Items.fireworks, 0, true));

}
