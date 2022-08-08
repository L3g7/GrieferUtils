package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderBurningCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;

@Singleton
public class NoFireOverlay extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Kein Feuer-Overlay")
            .description("§r§fDeaktiviert den Feuer-Effekt im First-Person-Modus.\n"
                    + "§l§nNur benutzen, wenn man Feuerresistenz besitzt!")
            .config("tweaks.no_fire_overlay.active")
            .icon("fire")
            .defaultValue(false);

    public NoFireOverlay() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onRender(RenderBurningCheckEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        event.setCanceled(player() == event.getEntity() && mc().gameSettings.thirdPersonView == 0);
    }
}
