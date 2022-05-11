package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.util.ChatComponentText;

@Singleton
public class RealMoney extends Feature {

    private final StringSetting tag = new StringSetting()
            .name("Tag")
            .icon(Material.NAME_TAG)
            .defaultValue("&a [✔]")
            .config("features.real_money.tag");

    private final RadioSetting<TagPosition> position = new RadioSetting<>(TagPosition.class)
            .name("Position")
            .icon("marker")
            .config("features.real_money.position")
            .defaultValue(TagPosition.AFTER)
            .stringProvider(TagPosition::getName);

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Realmoney")
            .icon("coin_pile")
            .defaultValue(false)
            .config("features.real_money.active")
            .subSettingsWithHeader("Realmoney", tag, position);

    public RealMoney() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener(priority = EventPriority.LOWEST)
    public void onMessageReceive(MessageReceiveEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        if (Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.getFormatted()).matches()) {
            String text = "§r" + tag.get().replace('&', '§') + "§r";

            if (position.get() == TagPosition.BEFORE)
                display(new ChatComponentText(text).appendSibling(event.getComponent()));
            else
                display(event.getComponent().appendText(text));

            event.setCanceled(true);
        }
    }

    private enum TagPosition {

        BEFORE("Davor"), AFTER("Danach");

        private final String name;

        TagPosition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
