package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class AutoSprint extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("AutoSprint")
            .config("tweaks.auto_sprint.active")
            .icon("speed")
            .defaultValue(true);

    public AutoSprint() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isActive())
            return;

        if (player() != null)
            player().setSprinting(true);
    }

}
