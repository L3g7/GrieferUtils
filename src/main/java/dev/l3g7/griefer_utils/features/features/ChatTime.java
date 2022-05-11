package dev.l3g7.griefer_utils.features.features;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


@Singleton
public class ChatTime extends Feature {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();
    private boolean styleValid = false;
    private boolean formatValid = false;

    private final StringSetting style = new StringSetting()
            .name("Design")
            .config("features.chat_time.style")
            .icon(Material.EMPTY_MAP)
            .callback(v -> styleValid = v.contains("%s"));

    private final StringSetting format = new StringSetting()
            .name("Zeitformat")
            .icon(Material.EMPTY_MAP)
            .config("features.chat_time.format")
            .callback(v -> {
                try {
                    DATE_FORMAT.applyPattern(v);
                    formatValid = true;
                } catch (IllegalArgumentException e) {
                    formatValid = false;
                }
            });

    private final BooleanSetting enabled = new BooleanSetting()
            .name("ChatTime")
            .config("features.chat_time.active")
            .icon(Material.WATCH)
            .defaultValue(false)
            .subSettingsWithHeader("ChatTime", style, format);

    public ChatTime() {
        super(Category.FEATURE);
        if(enabled.get() == null) { // If no value loaded, try loading from TebosBrime's addon
            File configFile = new File(mc().mcDataDir, "LabyMod/addons-1.8/config/ChatTime.json");
            if(configFile.exists()) {
                IOUtil.file(configFile).readJsonObject(obj -> {
                    JsonObject cfg = obj.get("config").getAsJsonObject();
                    format.defaultValue(cfg.has("chatData") ? cfg.get("chatData").getAsString() : "HH:mm:ss");
                    style.defaultValue(cfg.has("chatData2") ? cfg.get("chatData2").getAsString().replace("%time%", "%s") : "&4[&e%s&4a]");
                });
            }
        }
        format.defaultValue("HH:mm:ss");
        style.defaultValue("&7[&6%s&7] ");
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onMessageModifyChat(MessageModifyEvent event) {
        if (!styleValid || !formatValid || !isActive())
            return;

        event.setMessage(new ChatComponentText(String.format(style.get(), DATE_FORMAT.format(new Date())).replace('&', '§') + "§r").appendSibling(event.getMessage()));
    }

}