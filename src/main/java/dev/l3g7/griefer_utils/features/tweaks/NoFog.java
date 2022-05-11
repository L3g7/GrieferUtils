package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderFogCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

@Singleton
public class NoFog extends Feature {

    private static final BooleanSetting blindness = new BooleanSetting()
            .name("Keine Blindheit")
            .description("Deaktiviert den Blindheits-Effekt.")
            .config("tweaks.no_fog.blindness")
            .icon(new ItemStack(Blocks.stained_glass_pane, 1, 15))
            .defaultValue(false);

    private static final BooleanSetting water = new BooleanSetting()
            .name("Keine Wassertrübheit")
            .description("Deaktiviert die Wassertrübheit.")
            .config("tweaks.no_fog.water")
            .icon(new ItemStack(Blocks.stained_glass_pane, 1, 3))
            .defaultValue(false);

    private static final BooleanSetting lava = new BooleanSetting()
            .name("Keine Lavatrübheit")
            .description("Deaktiviert die Lavatrübheit.")
            .config("tweaks.no_fog.lava")
            .icon(new ItemStack(Blocks.stained_glass_pane, 1, 1))
            .defaultValue(false);

    private static final BooleanSetting enabled = new BooleanSetting()
            .name("Kein Nebel")
            .config("tweaks.no_fog.active")
            .icon(new ItemStack(Blocks.stained_glass_pane))
            .defaultValue(false)
            .subSettingsWithHeader("Kein Nebel", blindness, water, lava);

    public NoFog() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onRenderFogCheck(RenderFogCheckEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        switch (event.getType()) {
            case BLINDNESS:
                event.setCanceled(blindness.get());
                break;
            case WATER:
                event.setCanceled(water.get());
                break;
            case LAVA:
                event.setCanceled(lava.get());
                break;
        }
    }

}
