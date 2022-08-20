package dev.l3g7.griefer_utils.features.tweaks.item_info;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ItemInfo extends Feature {

	private static final List<ItemInfoSupplier> itemInfoSuppliers = FileProvider.getAllClasses()
			.filter(c -> Reflection.hasSuperclass(c, ItemInfoSupplier.class))
			.map(Reflection::loadClass)
			.map(ItemInfoSupplier.class::cast)
			.sorted()
			.collect(Collectors.toList());

	private final CategorySetting category = new CategorySetting()
			.name("Item-Infos")
			.description("Zeigt unterschiedliche Informationen unter einem Item an.")
			.icon("info")
			.subSettingsWithHeader("Item-Infos", itemInfoSuppliers.stream()
					.map(Feature::getMainElement)
					.toArray(SettingsElement[]::new));

	public ItemInfo() {
		super(Category.TWEAK);
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent e) {
		for (ItemInfoSupplier itemInfoSupplier : itemInfoSuppliers)
			e.toolTip.addAll(itemInfoSupplier.getToolTip(e.itemStack));
	}

	@Override
	public SettingsElement getMainElement() {
		return category;
	}
	
	public static abstract class ItemInfoSupplier extends Feature {

		public ItemInfoSupplier() {
			super(null);
		}

		public abstract SettingsElement getMainElement();

		public abstract List<String> getToolTip(ItemStack itemStack);

		@Override
		public boolean isActive() {
			return ((BooleanSetting) getMainElement()).get()
					&& Category.TWEAK.setting.get();
		}
	}
}