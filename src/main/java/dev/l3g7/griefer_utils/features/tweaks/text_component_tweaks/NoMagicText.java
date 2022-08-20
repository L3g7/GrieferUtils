package dev.l3g7.griefer_utils.features.tweaks.text_component_tweaks;

import dev.l3g7.griefer_utils.event.events.network.tablist.TabListEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.util.IChatComponent;


@Singleton
public class NoMagicText extends TextComponentTweak {

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Magischen Text deaktivieren")
            .description("§r§fDeaktiviert den magischen / verschleierten / verschlüsselten Stil in Chatnachrichten.")
            .config("tweaks.no_magic_text.active")
            .icon(Material.BLAZE_POWDER)
            .defaultValue(true)
            .callback(c -> TabListEvent.updatePlayerInfoList())
            .subSettingsWithHeader("Magischer Text", chat, tab, item);

    public NoMagicText() {
        super("no_magic_text");
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @Override
    void modify(IChatComponent component) {
        component.getChatStyle().setObfuscated(false);
        component.getSiblings().forEach(this::modify);
    }

    @Override
    String modify(String message) {
        return message.replace("§k", "");
    }

}