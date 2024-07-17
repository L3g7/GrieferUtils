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

	String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};

	RecraftRecordingCore getCore();

	void setIcon(ItemStack stack);
	void updateStartRecordingIcon(String icon);

	boolean playSuccessor();

	default List<RecraftAction> actions() { return getCore().actions; }
	default StringSetting name() { return getCore().name; }
	default KeySetting key() { return getCore().key; }
	default SwitchSetting ignoreSubIds() { return getCore().ignoreSubIds; }
	default DropDownSetting<RecordingMode> mode() { return getCore().mode; }
	default SwitchSetting craftAll() { return getCore().craftAll; }
	default void updateStartRecordingIcon() { updateStartRecordingIcon(actions().isEmpty() ? "recording_red" : "recording_white"); }

}
