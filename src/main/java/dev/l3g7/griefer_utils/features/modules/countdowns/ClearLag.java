package dev.l3g7.griefer_utils.features.modules.countdowns;


import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ClearLag extends Module {

    private final BooleanSetting shorten = new BooleanSetting()
            .name("Zeit kürzen")
            .icon(Material.LEVER)
            .config("modules.clear_lag.shorten")
            .defaultValue(false);

    private final BooleanSetting warn = new BooleanSetting()
            .name("Warnen")
            .icon(Material.LEVER)
            .config("modules.clear_lag.warn")
            .defaultValue(false);

    private long clearLagEnd = -1;

    public ClearLag() {
        super("Clearlag", "Zeigt dir die Zeit bis zum nächsten Clearlag an", "clearlag", new IconData(Material.LAVA_BUCKET));
    }

    @Override
    public String[] getValues() {
        if (clearLagEnd == -1)
            return getDefaultValues();

        long diff = clearLagEnd - System.currentTimeMillis();
        if (diff < 0)
            return getDefaultValues();

        // Warn if clearlag is less than 20s away
        if (warn.get() && diff < 20 * 1000) {
            String s = RenderUtil.formatTime(clearLagEnd, true);
            if (!s.equals("0s"))
                title("§c§l" + s);
        }

        return new String[]{RenderUtil.formatTime(clearLagEnd, shorten.get())};
    }

    @Override
    public String[] getDefaultValues() {
        return new String[]{"Unbekannt"};
    }

    @EventListener
    public void onServerSwitch(ServerSwitchEvent event) {
        clearLagEnd = -1;
    }

    @EventListener
    public void onMMCustomPayload(MMCustomPayloadEvent event) {
        if (!event.getChannel().equals("countdown_create"))
            return;

        JsonObject countdown = event.getPayload().getAsJsonObject();
        if (countdown.get("name").getAsString().equals("ClearLag")) {
            clearLagEnd = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(countdown.get("until").getAsInt(), TimeUnit.valueOf(countdown.get("unit").getAsString()));
        }
    }

    @Override
    public void fillSubSettings(List<SettingsElement> list) {
        super.fillSubSettings(list);
        list.add(shorten);
        list.add(warn);
    }

    private void title(String title) {
        mc.ingameGUI.displayTitle("§cClearlag!", null, -1, -1, -1);
        mc.ingameGUI.displayTitle(null, title, -1, -1, -1);
        mc.ingameGUI.displayTitle(null, null, 0, 2, 3);
    }

}
