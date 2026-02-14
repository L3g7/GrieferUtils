package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.Category;

import java.util.List;

/**
 * {@link Feature} or {@link Feature.CategoryData}.
 */
public interface GUIEntry {
	String name();

	void addToParent(List<BaseSetting<?>> root);

	interface SettingBuilder {
		BaseSetting<?> build(Category meta, String configKey);
	}

	@Singleton
	class SwitchSettingBuilder implements SettingBuilder {
		@Override
		public SwitchSetting build(Category meta, String configKey) {
			SwitchSetting setting = SwitchSetting.create()
				.name(meta.name())
				.icon(meta.icon())
				.config(configKey + ".active")
				.defaultValue(true)
				.subSettings(); // creates a header

			if (!meta.description().isEmpty())
				setting.description(meta.description());

			return setting;
		}
	}
}
