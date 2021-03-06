package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class ToolSaver extends Feature {

    private final NumberSetting damage = new NumberSetting()
            .name("ToolSaver")
            .description("Deaktiviert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.\n" +
                         "(0 zum Deaktivieren)")
            .icon("broken_pickaxe")
            .defaultValue(0)
            .config("features.tool_saver.active");

    public ToolSaver() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return damage;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (damage.get() == 0 || !isCategoryEnabled())
            return;

        if (player() == null)
            return;

        if ((event.button != 0 && event.button != 1) || !event.buttonstate)
            return;

        ItemStack heldItem = player().getHeldItem();
        if (heldItem == null || heldItem.getMaxDamage() == 0)
            return;

        if (damage.get() >= heldItem.getMaxDamage() - heldItem.getItemDamage())
            event.setCanceled(true);
    }
}
