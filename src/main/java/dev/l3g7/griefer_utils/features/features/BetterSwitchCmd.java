package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class BetterSwitchCmd extends Feature {

    private boolean waitingForGui = false;
    private int slot = 0;

    private final BooleanSetting enabled = new BooleanSetting()
            .name("BetterSwitchCmd")
            .config("features.better_switch_cmd.active")
            .description("Ermöglicht das direkte Joinen auf einen Citybuild mit \"/switch [CB]\", \"/cb [CB]\" oder \"/cb[CB]\".")
            .icon(Material.COMPASS)
            .defaultValue(false);

    public BetterSwitchCmd() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onMessageSend(MessageSendEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        String msg = event.getMsg().toLowerCase();
        
        if (msg.matches("^/(?:cb ?|switch )(?:n|nature)$"))
            join(23);
        else if (msg.matches("^/(?:cb ?|switch )(?:x|extreme)$"))
            join(24);
        else if (msg.matches("^/(?:cb ?|switch )(?:e|evil)$"))
            join(25);
        else if (msg.matches("^/(?:cb ?|switch )(\\d+)$"))
            join(Integer.parseInt(msg.replaceAll("^/(?:cb ?|switch )(\\d+)$", "$1")));
        else
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!waitingForGui || !(event.gui instanceof GuiChest))
            return;

        IInventory inv = Reflection.get(event.gui, "lowerChestInventory", "field_147015_w", "w");
        if (!inv.getDisplayName().getUnformattedText().equals("§6§lServerwechsel"))
            return;

        TickScheduler.runNextTick(() -> {
            mc().playerController.windowClick(player().openContainer.windowId, slot, 0, 0, player());
            waitingForGui = false;
        });

    }

    public void join(int cb) {
        if (cb < 1 || cb > 25) {
            display(Constants.ADDON_PREFIX + "Citybuild %d existiert nicht!", cb);
            return;
        }
        cb--;
        int row = cb / 7;
        int column = cb % 7;
        slot = (row * 9 + column) + 10;

        send("/switch");
        waitingForGui = true;
    }

}
