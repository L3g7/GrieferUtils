package dev.l3g7.griefer_utils.features.tweaks.scoreboard_info;

import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import static dev.l3g7.griefer_utils.features.modules.OrbBalance.DECIMAL_FORMAT;
import static dev.l3g7.griefer_utils.features.modules.OrbBalance.getBalance;

@Singleton
public class OrbScoreboard extends ScoreboardHandler.ScoreboardMod {

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Orbguthaben im Scoreboard")
		.config("tweaks.orb_balance.active")
		.icon(Material.EXP_BOTTLE)
		.defaultValue(true);

	public OrbScoreboard() {
		super("Orbguthaben", 0);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@Override
	protected String getValue() {
		long balance = getBalance();
		return balance == -1 ? "?" : DECIMAL_FORMAT.format(balance);
	}

}
