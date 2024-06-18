package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore.RecordingMode;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Has to be an interface because in LabyMod 4 the RecraftRecording has to extend Config
 */
public interface RecraftRecording {

	RecraftRecordingCore getCore();

	void setIcon(ItemStack stack);

	boolean playSuccessor();

	default List<RecraftAction> actions() { return getCore().actions; }
	default StringSetting name() { return getCore().name; }
	default KeySetting key() { return getCore().key; }
	default SwitchSetting ignoreSubIds() { return getCore().ignoreSubIds; }
	default DropDownSetting<RecordingMode> mode() { return getCore().mode; }
	default SwitchSetting craftAll() { return getCore().craftAll; }

}
