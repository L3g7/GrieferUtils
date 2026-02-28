package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;

public class UpdateInfoSettings {

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Update-Infos anzeigen")
		.description("Ob die NEW-Plakette anzeigt werden soll, wenn seit der letzten installierten Version etwas hinzugefügt wurde.")
		.icon("bell")
		.config("settings.update_infos")
		.defaultValue(true);

}
