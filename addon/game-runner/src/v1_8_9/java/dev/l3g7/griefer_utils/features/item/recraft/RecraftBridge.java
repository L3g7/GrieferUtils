package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;

@Bridged
public interface RecraftBridge {

	RecraftBridge recraftBridge = FileProvider.getBridge(RecraftBridge.class);

	BaseSetting<?> getPagesSetting();

	void openPieMenu(boolean animation);

	void closePieMenu();

	RecraftRecording createEmptyRecording();

	default void init() {}

}
