package dev.l3g7.griefer_utils.features.modules.money;


import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;

@Singleton
public class Received extends Module {

    static BigDecimal moneyReceived = BigDecimal.ZERO;

    public Received() {
        super("Bekommen", "Zeigt dir, wie viel Geld du seit Minecraft-Start bekommen hast", "received", new IconData(Material.EMERALD));
    }

    @EventListener
    public void onMessageReceive(MessageReceiveEvent event) {
        Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.getFormatted());
        if (matcher.matches()) {
            moneyReceived = moneyReceived.add(new BigDecimal(matcher.group("amount").replace(".", "").replace(",", ".")));
        }
    }

    @Override
    public void fillSubSettings(List<SettingsElement> list) {
        list.add(new ButtonSetting().name("Zurücksetzen").callback(() -> moneyReceived = BigDecimal.ZERO));
        list.add(new ButtonSetting().name("Alles zurücksetzen").callback(() -> moneyReceived = Spent.moneySpent = BigDecimal.ZERO));
    }

    @Override
    public String[] getValues() {
        return new String[]{Constants.DECIMAL_FORMAT_98.format(moneyReceived) + "$"};
    }

    @Override
    public String[] getDefaultValues() {
        return new String[]{"0$"};
    }
}
