package dev.l3g7.griefer_utils.features.modules.money;


import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;

import static java.math.BigDecimal.ZERO;

/*
 * Not the cleanest code, but it will be redone in v2 anyway :P
 */
@Singleton
public class Spent extends Module {

	private static final int HOUR = 60 * 60 * 1000; // An hour, in milliseconds.

    static BigDecimal moneySpent = BigDecimal.ZERO;
	private long nextReset = -1;

	private final BooleanSetting resetSetting = new BooleanSetting()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04.00 das eingenommene Geld zurückgesetzt werden soll.")
		.defaultValue(false)
		.callback(b -> {
			if (!b) {
				nextReset = -1;
			} else {
				if (nextReset == -1)
					nextReset = getNextReset();
				else
					nextReset = Math.min(nextReset, getNextReset());
			}
			Config.set("modules.money.data." + PlayerUtil.getUUID() + ".next_reset", nextReset);
			Config.save();
		});

    public Spent() {
        super("Ausgegeben", "Zeigt dir, wie viel Geld du seit Minecraft-Start ausgegeben hast", "spent", new IconData(Material.COAL));
    }

    @Override
    public void fillSubSettings(List<SettingsElement> list) {
	    list.add(resetSetting);
        list.add(new ButtonSetting().name("Zurücksetzen").callback(() -> setBalance(BigDecimal.ZERO)));
        list.add(new ButtonSetting().name("Alles zurücksetzen").callback(() -> setBalance(Received.setBalance(BigDecimal.ZERO))));
    }

    @Override
    public String[] getValues() {
        return new String[]{Constants.DECIMAL_FORMAT_98.format(moneySpent) + "$"};
    }

    @Override
    public String[] getDefaultValues() {
        return new String[]{"0$"};
    }

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.PAYMENT_SEND_PATTERN.matcher(event.getFormatted());
		if (matcher.matches())
			setBalance(moneySpent.add(new BigDecimal(matcher.group("amount").replace(",", ""))));
	}

	private long getNextReset() {
		long time = System.currentTimeMillis();
		long reset = time - time % (24 * HOUR) + (2 * HOUR); // Get timestamp for 02:00 UTC on the current day

		if (System.currentTimeMillis() > reset)
			reset += 24 * HOUR; // When it's already after 02:00 UTC, the next reset is 24h later

		return reset;
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextReset();
			Config.set("modules.money.data." + PlayerUtil.getUUID() + ".next_reset", nextReset);
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener
	public void loadBalance(ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		String path = "modules.money.balances." + PlayerUtil.getUUID() + ".";

		if (Config.has(path + "spent"))
			setBalance(BigDecimal.valueOf(Config.get(path + "spent").getAsLong()));
		if (Config.has(path + "next_reset")) {
			nextReset = Config.get(path + "next_reset").getAsLong();
			resetSetting.set(nextReset != -1);
		}
	}

	protected static BigDecimal setBalance(BigDecimal newValue) {
		moneySpent = newValue;
		// Save balance along with player uuid so no problems occur when using multiple accounts
		Config.set("modules.money.balances." + PlayerUtil.getUUID() + ".spent", moneySpent.doubleValue());
		Config.save();
		return newValue;
	}
}
