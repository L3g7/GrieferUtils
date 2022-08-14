package dev.l3g7.griefer_utils.features.tweaks.item_info.grieferwert;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.features.tweaks.item_info.ItemInfo.ItemInfoSupplier;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class GrieferWert extends ItemInfoSupplier {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("GrieferWert")
            .icon("coin_pile")
            .defaultValue(false)
            .config("tweaks.item_info.griefer_wert.active");

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

    @Override
    public List<String> getToolTip(ItemStack itemStack) {
        if (!isActive() || !isOnGrieferGames() || gwEntries == null)
            return Collections.emptyList();

        List<String> gwEntries = new ArrayList<>();

        // Populate gwEntries
        this.gwEntries.stream()
                .filter(g -> g.testItem(itemStack))
                .map(GWEntry::toTooltipString)
                .forEach(gwEntries::add);

        // Add gwEntries to tooltip
        if (gwEntries.isEmpty())
            return Collections.emptyList();

        List<String> toolTip = new ArrayList<>();

        toolTip.add("§r");
        toolTip.add("§a§lGrieferwert:");
        toolTip.addAll(gwEntries);

        return toolTip;
    }
}
