package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

@Singleton
public class ChatMods extends Feature {

    private final BooleanSetting antiClearChat = new BooleanSetting()
            .name("AntiClearChat")
            .icon(Material.BARRIER)
            .defaultValue(true)
            .config("tweaks.chat_mods.anti_clear_chat.active");

    private final BooleanSetting removeSupremeSpaces = new BooleanSetting()
            .name("Supreme-Leerzeichen entfernen")
            .icon(Material.BARRIER)
            .defaultValue(true)
            .config("tweaks.chat_mods.remove_supreme_spaces.active");

    private final RadioSetting<NewsMode> news = new RadioSetting<>(NewsMode.class)
            .name("News")
            .icon("exclamation_mark")
            .defaultValue(NewsMode.NORMAL)
            .config("tweaks.chat_mods.news.mode")
            .stringProvider(NewsMode::getName);

    private final CategorySetting category = new CategorySetting()
            .name("ChatMods")
            .icon("speech_bubble")
            .subSettingsWithHeader("ChatMods", antiClearChat, removeSupremeSpaces, news);

    public ChatMods() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return category;
    }

    private enum NewsMode {

        NONE("Versteckt"), COMPACT("Kompakt"), NORMAL("Normal");

        private final String name;

        NewsMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private boolean isNews = false;

    @EventListener
    public void onMessageReceive(MessageReceiveEvent event) {
        if (!isCategoryEnabled() || !isOnGrieferGames())
            return;

        boolean isNewsLine = event.getFormatted().equals("§r§f§m------------§r§8 [ §r§6News§r§8 ] §r§f§m------------§r");
        if (isNewsLine)
            isNews = !isNews;

        // News mode
        if (news.get() == NewsMode.NONE)
            event.setCanceled(isNews || isNewsLine);
        else if (news.get() == NewsMode.COMPACT)
            event.setCanceled(isNewsLine || (isNews && event.getFormatted().trim().equals("§r§8\u00bb§r")));

        // Anti clear chat
        if (!event.isCanceled())
            event.setCanceled(antiClearChat.get() && event.getFormatted().replaceAll("§.", "").trim().isEmpty());

        // remove supreme spaces
        if (!event.isCanceled())
            event.setCanceled(removeSupremeSpaces.get() && event.getFormatted().trim().equals("§r§8\u00bb§r"));
    }
}
