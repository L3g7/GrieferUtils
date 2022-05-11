package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListEvent;
import dev.l3g7.griefer_utils.event.events.render.DisplayNameRenderEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.util.IChatComponent;

@Singleton
public class NameTagPrefixSync extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Nametag mit Prefix")
            .description("Färbt den Namen über dem Kopf, dass er zum ausgewählten Prefix passt.")
            .config("tweaks.name_tag_prefix_sync.active")
            .icon("rainbow_name")
            .defaultValue(true);

    public NameTagPrefixSync() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onDisplayNameRender(DisplayNameRenderEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        IChatComponent component = TabListEvent.cachedComponents.get(event.getPlayer().getUniqueID());
        if(component != null)
            event.setDisplayName(component);
    }

}