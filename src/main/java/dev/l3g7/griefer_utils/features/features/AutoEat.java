package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class AutoEat extends Feature {

    private final DropDownSetting<TriggerMode> triggerMode = new DropDownSetting<>(TriggerMode.class)
            .name("Auslösung")
            .description("Wann AutoEat essen soll.\n" +
                    "Wenn effizient ausgewählt ist, wird gegessen, wenn kein Sättigungspunkt des Essens verschwendet wird.")
            .defaultValue(TriggerMode.EFFICIENTLY)
            .config("features.auto_eat.trigger_mode")
            .stringProvider(TriggerMode::getName);

    private final DropDownSetting<PreferredFood> preferredFood = new DropDownSetting<>(PreferredFood.class)
            .name("Bevorzugte Nahrung")
            .defaultValue(PreferredFood.HIGH_SATURATION)
            .config("features.auto_eat.preferred_food")
            .stringProvider(PreferredFood::getName);

    private final BooleanSetting enabled = new BooleanSetting()
            .name("AutoEat")
            .config("features.auto_eat.active")
            .icon(Material.COOKED_BEEF)
            .defaultValue(false)
            .subSettingsWithHeader("AutoEat", triggerMode, preferredFood);

    public AutoEat() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    private int previousHotbarSlot;
    private boolean waiting = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isActive() || !isOnGrieferGames()) return;
        if (waiting) return;

        if (player() == null) return;
        if (player().getItemInUse() != null) return;
        if (!player().canEat(false)) return;

        int itemIndex = getSlotOfFood();
        if (itemIndex == -1) return;

        KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), true);
        previousHotbarSlot = player().inventory.currentItem;
        player().inventory.currentItem = itemIndex;
    }

    @SubscribeEvent
    public void onUseItemFinish(PlayerUseItemEvent.Finish event) {
        waiting = true;
        // Need to wait some ticks, maybe because of NCP?
        TickScheduler.runLater(() -> {
            KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), false);
            TickScheduler.runNextTick(() -> player().inventory.currentItem = previousHotbarSlot);
            waiting = false;
        }, 5);
    }

    /**
     * Get hotbar slot with the best food
     */
    private int getSlotOfFood() {
        int hunger = 20 - player().getFoodStats().getFoodLevel();
        if (hunger == 0) return -1;

        int foundIndex = -1;
        int foundSaturation = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack item = player().inventory.getStackInSlot(i);
            if (item == null || item.getItem() == null || !(item.getItem() instanceof ItemFood))
                continue;

            int saturation = ((ItemFood) item.getItem()).getHealAmount(item);
            if (foundIndex == -1 // Check if food found better than current food
                    || (preferredFood.get() == PreferredFood.HIGH_SATURATION && foundSaturation < saturation)
                    || (preferredFood.get() == PreferredFood.LOW_SATURATION && foundSaturation > saturation)) {
                foundSaturation = saturation;
                foundIndex = i;
            }
        }
        // Check trigger mode
        return (triggerMode.get() == TriggerMode.HALF_BAR || hunger >= foundSaturation) ? foundIndex : -1;
    }

    private enum TriggerMode {

        HALF_BAR("Bei halbem Hungerbalken"), EFFICIENTLY("Effizient");

        final String name;

        TriggerMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum PreferredFood {

        HIGH_SATURATION("stark sättigende Nahrung"), LOW_SATURATION("schwach sättigende Nahrung");

        final String name;

        PreferredFood(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
