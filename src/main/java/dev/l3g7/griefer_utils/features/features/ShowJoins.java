package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListAddPlayerEvent;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListRemovePlayerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringListSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

@Singleton
public class ShowJoins extends Feature {

    private final StringListSetting filterData = new StringListSetting()
            .name("%s. Spieler")
            .icon("steve")
            .config("features.show_joins.filter.data");

    private final BooleanSetting filter = new BooleanSetting()
            .name("Filter")
            .icon(Material.HOPPER)
            .config("features.show_joins.filter.active")
            .defaultValue(false)
            .subSettingsWithHeader("Joins anzeigen - Filter",
                    filterData);

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Joins anzeigen")
            .description("Zeigt dir an, wenn (bestimmte) Spieler den Server betreten / verlassen.")
            .icon("radar")
            .config("features.show_joins.active")
            .defaultValue(false)
            .subSettingsWithHeader("Joins anzeigen", filter);

    public ShowJoins() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    private boolean isNameInFilter(String name) {
        if (Feature.name().equals(name)) // Don't show Joins/Leaves for yourself
            return false;

        if(filter.get())
            return filterData.getValues().stream().anyMatch(s -> s.equalsIgnoreCase(name));

        return name != null && !name.contains("§"); // All NPCs' names on GrieferGames contain §
    }

    @EventListener
    public void onJoin(TabListAddPlayerEvent event) {
        if (!isActive() || !isOnGrieferGames() || !isOnCityBuild())
            return;


        if (isNameInFilter(event.getName())) // Can't call display on network thread (concurrent modification)
            TickScheduler.runNextRenderTick(() -> display(Constants.ADDON_PREFIX + "§8[§a+§8] §r" + event.getName()));
    }

    @EventListener
    public void onQuit(TabListRemovePlayerEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        if (isNameInFilter(event.getName())) // Can't call display on network thread (concurrent modification)
            TickScheduler.runNextRenderTick(() -> display(Constants.ADDON_PREFIX + "§r§8[§c-§8] §r" + event.getName()));
    }

}
