package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Singleton
public class BonzeSaver extends Feature {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("BonzeSaver")
            .description("Deaktiviert Linksklicks, wenn eine Bonzeklinge in der Hand gehalten wird.")
            .icon(new ItemBuilder(Items.diamond_sword).enchant())
            .defaultValue(true)
            .config("features.bonze_saver.active");

    public BonzeSaver() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        // Only disable left clicks
        if (event.button != 0 || !event.buttonstate)
            return;

        if(player() == null)
            return;

        InventoryPlayer inv = player().inventory;
        if(inv.getCurrentItem() == null)
            return;

        // Check by tooltip
        List<String> tooltip = inv.getCurrentItem().getTooltip(player(), false);
        for(int i = 0; i < tooltip.size() - 1; i++) {
            if(tooltip.get(i).replaceAll("§.", "").equals("Edle Klinge von GrafBonze") && tooltip.get(i + 1).replaceAll("§.", "").startsWith("Signiert von GrafBonze am ")) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
