package dev.l3g7.griefer_utils.features.tweaks.better_sign;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class BetterSign extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Bessere Schilder")
            .config("tweaks.better_sign.active")
            .description("Implementiert folgendes im Schilder-Bearbeitungsmenü:\n- Cursorposition\n- Auswahl\n- Copy - Paste")
            .icon(Material.SIGN)
            .defaultValue(true);

    public BetterSign() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isActive())
            return;

        if (event.gui instanceof GuiEditSign)
            event.gui = new BetterGuiEditSign((GuiEditSign) event.gui);
    }
}
