package dev.l3g7.griefer_utils.features.modules;


import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.utils.Material;

@Singleton
public class Redstone extends Module {

    private int redstoneState = -1;

    public Redstone() {
        super("Redstone", "Zeigt dir den Redstonestatus an.", "redstone", new IconData(Material.REDSTONE));
    }

    @EventListener
    public void onMMCustomPayload(MMCustomPayloadEvent event) {
        if (!event.getChannel().equals("redstone"))
            return;

        redstoneState = event.getPayload().getAsJsonObject().get("status").getAsInt();
    }

    @Override
    public String[] getValues() {
        if (mc.theWorld == null)
            return getDefaultValues();

        switch (redstoneState) {
            case -1:
                return new String[]{"Unbekannt"};
            case 0:
                return new String[]{"§aAktiviert"};
            default:
                return new String[]{"§4Deaktiviert"};
        }
    }

    @Override
    public String[] getDefaultValues() {
        return new String[]{"Unbekannt"};
    }
}
