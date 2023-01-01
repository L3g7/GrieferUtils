/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.modules.money;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.math.BigDecimal.ZERO;

/*
 * Not the cleanest code, but it will be redone in v2 anyway :P
 */
@Singleton
public class Spent extends Module {

	private static final int HOUR = 60 * 60 * 1000; // An hour, in milliseconds.
	public static final Pattern PAYMENT_SEND_PATTERN = Pattern.compile(String.format("^§r§aDu hast %s§r§a \\$(?<amount>[\\d.,]+) gegeben\\.§r$", Constants.FORMATTED_PLAYER_PATTERN));

    static BigDecimal moneySpent = BigDecimal.ZERO;
	private long nextReset = -1;

	private final BooleanSetting resetSetting = new BooleanSetting()
		.name("Automatisch zurücksetzen")
		.description("Ob automatisch um 04:00 das eingenommene Geld zurückgesetzt werden soll.")
		.icon(ModTextures.SETTINGS_DEFAULT_USE_DEFAULT_SETTINGS)
		.callback(b -> {
			if (!b) {
				nextReset = -1;
			} else {
				if (nextReset == -1)
					nextReset = getNextReset();
				else
					nextReset = Math.min(nextReset, getNextReset());
			}
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			Config.save();
		});

    public Spent() {
        super("Ausgegeben", "Zeigt dir, wie viel Geld du seit Minecraft-Start ausgegeben hast", "spent", new IconData("griefer_utils/icons/wallet_outgoing.png"));
    }

    @Override
    public void fillSubSettings(List<SettingsElement> list) {
	    super.fillSubSettings(list);
	    list.add(resetSetting);
	    list.add(new SmallButtonSetting()
		    .name("Zurücksetzen")
		    .icon("arrow_circle")
		    .buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
		    .callback(() -> setBalance(ZERO)));

	    list.add(new SmallButtonSetting()
		    .name("Alles zurücksetzen")
		    .icon("arrow_circle")
		    .buttonIcon(new IconData(ModTextures.BUTTON_TRASH))
		    .callback(() -> setBalance(Spent.setBalance(ZERO))));
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
	public void onMessageReceive(ClientChatReceivedEvent event) {
		Matcher matcher = PAYMENT_SEND_PATTERN.matcher(event.message.getFormattedText());
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

	@EventListener
	public void onTick(TickEvent.ClientTickEvent tickEvent) {
		if (nextReset != -1 && System.currentTimeMillis() > nextReset ) {
			nextReset = getNextReset();
			Config.set("modules.money.data." + mc.getSession().getProfile().getId() + ".next_reset", new JsonPrimitive(nextReset));
			setBalance(ZERO);
			Config.save();
		}
	}

	@EventListener
	public void loadBalance(ServerEvent.ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		String path = "modules.money.balances." + mc.getSession().getProfile().getId() + ".";

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
		Config.set("modules.money.balances." + mc.getSession().getProfile().getId() + ".spent", new JsonPrimitive(moneySpent));
		Config.save();
		return newValue;
	}
}