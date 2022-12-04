package dev.l3g7.griefer_utils.features.features.item_saver;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.tweaks.item_info.ItemInfo;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ItemSaver extends Feature {

	private static final List<ItemSaverImpl> itemSaverImpls = FileProvider.getAllClasses()
		.filter(c -> Reflection.hasSuperclass(c, ItemSaverImpl.class))
		.map(Reflection::loadClass)
		.map(ItemSaverImpl.class::cast)
		.sorted()
		.collect(Collectors.toList());

	private final CategorySetting category = new CategorySetting()
		.name("Item-Saver")
		.description("Rettet unterschiedliche Items.")
		.icon("shield")
		.subSettingsWithHeader("Item-Saver", FileProvider.getAllClasses()
			.filter(c -> Reflection.hasSuperclass(c, ItemSaverImpl.class))
			.map(Reflection::loadClass)
			.map(ItemSaverImpl.class::cast)
			.sorted()
			.map(Feature::getMainElement)
			.toArray(SettingsElement[]::new));

	public ItemSaver() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return category;
	}

	public static abstract class ItemSaverImpl extends Feature {

		public ItemSaverImpl() {
			super(null);
		}

		public abstract SettingsElement getMainElement();

		@Override
		public boolean isCategoryEnabled() {
			return Category.FEATURE.setting.get();
		}
	}
}