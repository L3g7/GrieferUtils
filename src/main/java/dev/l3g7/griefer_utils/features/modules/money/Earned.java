package dev.l3g7.griefer_utils.features.modules.money;


import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class Earned extends Module {

	public Earned() {
		super("Verdient", "Zeigt dir, wie viel Geld du seit Minecraft-Start verdient hast", "earned", new IconData(Material.IRON_INGOT));
	}

	@Override
	public String[] getValues() {
		return new String[]{Constants.DECIMAL_FORMAT_98.format(Received.moneyReceived.subtract(Spent.moneySpent)) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

	@Override
	public boolean isShown() {
		return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames();
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		list.add(new ButtonSetting().name("Zurücksetzen").callback(() -> {
			Received.moneyReceived = BigDecimal.ZERO;
			Spent.moneySpent = BigDecimal.ZERO;
		}));
	}

}

