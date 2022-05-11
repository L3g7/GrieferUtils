package dev.l3g7.griefer_utils.features.features.grieferwert;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class GrieferWert extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("GrieferWert")
            .icon("coin_pile")
            .defaultValue(false)
            .config("features.griefer_wert.active");

    public GrieferWert() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    private List<GWEntry> gwEntries = new ArrayList<>();

    @OnEnable
    public void loadPrices() {
        IOUtil.request("http://server1.mysterymod.net:5600/api/v1/itemwert/griefergames").asJsonArray(array -> {
            for (JsonElement entry : array)
                gwEntries.add(new GWEntry(entry.getAsJsonObject()));
        }).orElse(error -> {
            gwEntries = null;
            enabled .name("§c§mGrieferWert")
                    .description("§cGrieferWert konnte nicht geladen werden!");
        });
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent e) {
        if (!isActive() || !isOnGrieferGames() || gwEntries == null)
            return;

        List<String> gwEntries = new ArrayList<>();

        // Populate gwEntries
        this.gwEntries.stream()
                .filter(g -> g.testItem(e.itemStack))
                .map(GWEntry::toTooltipString)
                .forEach(gwEntries::add);

        // Add gwEntries to tooltip
        if (!gwEntries.isEmpty()) {
            e.toolTip.add("§r");
            e.toolTip.add("§a§lGrieferwert:");
            e.toolTip.addAll(gwEntries);
        }
    }
}
