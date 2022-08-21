package dev.l3g7.griefer_utils.features.tweaks;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.List;

@Singleton
public class ChatMods extends Feature {

    private static final List<String> MYSTERY_MOD_DOWNLOAD_NOTIFICATION = ImmutableList.of(
            "§r§8[§r§6GrieferGames§r§8] §r§cOhje. Du benutzt noch kein MysteryMod!§r",
            "§r§8[§r§6GrieferGames§r§8] §r§fWir sind optimiert für MysteryMod und die neusten Funktionen hast Du nur damit.§r",
            "§r§8[§r§6GrieferGames§r§8] §r§fDownload: §r§ahttps://mysterymod.net/download/§r"
    );

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

    private final BooleanSetting removeStreamerNotifications = new BooleanSetting()
            .name("Streamer-Benachrichtigungen entfernen")
            .icon("twitch")
            .defaultValue(false)
            .config("tweaks.chat_mods.remove_streamer.active");

    private final BooleanSetting stfuMysteryMod = new BooleanSetting()
            .name("Download-Benachrichtigungen entfernen")
            .icon("mysterymod")
            .defaultValue(true)
            .config("tweaks.chat_mods.stfu_mysterymod.active");

    private final CategorySetting category = new CategorySetting()
            .name("ChatMods")
            .icon("speech_bubble")
            .subSettingsWithHeader("ChatMods", antiClearChat, removeSupremeSpaces, removeStreamerNotifications, stfuMysteryMod, news);

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

        // remove streamer
        if (!event.isCanceled())
            event.setCanceled(removeStreamerNotifications.get() && event.getFormatted().startsWith("§8[§6Streamer§8]"));

        // Remove MysteryMod download notification
        if (!event.isCanceled())
            event.setCanceled(stfuMysteryMod.get() && MYSTERY_MOD_DOWNLOAD_NOTIFICATION.contains(event.getFormatted()));
    }
}
