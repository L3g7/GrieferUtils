package dev.l3g7.griefer_utils.features.tweaks.scoreboard_info;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;

@Singleton
public class BankScoreboard extends ScoreboardHandler.ScoreboardMod {

    private static long bankBalance = -1;

	public static long getBankBalance() {
		return bankBalance;
	}

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Bankguthaben im Scoreboard")
            .config("tweaks.bank_scoreboard.active")
            .icon("bank")
            .defaultValue(true);

	public BankScoreboard() {
		super("Bankguthaben", 1);
	}

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onMMCustomPayload(MMCustomPayloadEvent event) {
        if (event.getChannel().equals("bank"))
            bankBalance = event.getPayload().getAsJsonObject().get("amount").getAsLong();
    }

	@Override
	protected String getValue() {
		return bankBalance == -1 ? "?" : Constants.DECIMAL_FORMAT_98.format(bankBalance) + "$";
	}

}
