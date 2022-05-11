package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class FullBright extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("FullBright")
            .config("tweaks.full_bright.active")
            .icon("light_bulb")
            .defaultValue(true);

    public FullBright() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(isActive())
            mc().gameSettings.gammaSetting = 10;
    }

}
