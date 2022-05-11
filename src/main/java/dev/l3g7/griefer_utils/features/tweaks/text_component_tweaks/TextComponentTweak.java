package dev.l3g7.griefer_utils.features.tweaks.text_component_tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListEvent;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

abstract class TextComponentTweak extends Feature {

    final BooleanSetting chat = new BooleanSetting()
            .name("In Chat")
            .icon("speech_bubble")
            .defaultValue(true);

    final BooleanSetting tab = new BooleanSetting()
            .name("In Tabliste")
            .icon("rainbow_name")
            .defaultValue(true)
            .callback(c -> updatePlayerInfoList());

    final BooleanSetting item = new BooleanSetting()
            .name("In Item-Beschreibungen")
            .icon(Material.GOLD_INGOT)
            .defaultValue(false);

    TextComponentTweak(String configName) {
        super(Category.TWEAK);
        item.config("tweaks." + configName + ".item");
        tab.config("tweaks." + configName + ".tab");
        chat.config("tweaks." + configName + ".chat");
    }

    void updatePlayerInfoList() {
        if (mc().getNetHandler() == null)
            return;

        for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
            IChatComponent originalComponent = TabListEvent.cachedComponents.get(info.getGameProfile().getId());
            IChatComponent component = EventBus.post(new TabListNameUpdateEvent(info.getGameProfile(), originalComponent)).getComponent();
            info.setDisplayName(component);
        }
    }

    @EventListener
    public void onPacket(TabListNameUpdateEvent event) {
        if (!tab.get() || !isActive())
            return;

        modify(event.getComponent());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e) {
        if (!item.get() || !isActive())
            return;

        for (int i = 0; i < e.toolTip.size(); i++)
            e.toolTip.set(i, modify(e.toolTip.get(i)));
    }

    @EventListener
    public void onMessageModifyChat(MessageModifyEvent event) {
        if (!chat.get() || !isActive())
            return;

        modify(event.getMessage());
    }

    abstract void modify(IChatComponent component);

    abstract String modify(String message);

}