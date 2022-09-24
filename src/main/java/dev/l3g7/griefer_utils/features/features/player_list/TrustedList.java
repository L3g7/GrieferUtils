package dev.l3g7.griefer_utils.features.features.player_list;

import dev.l3g7.griefer_utils.event.events.network.tablist.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class TrustedList extends PlayerList {

    private final RadioSetting<MarkAction> tabAction = new RadioSetting<>(MarkAction.class)
            .name("in Tabliste")
            .icon("tab_list")
            .config("features.trusted_list.tab")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName)
            .callback(a -> TabListNameUpdateEvent.updateTabListNames());

    private final RadioSetting<MarkAction> chatAction = new RadioSetting<>(MarkAction.class)
            .name("in Chat")
            .icon("speech_bubble")
            .config("features.trusted_list.chat")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName);

    private final RadioSetting<MarkAction> displayNameAction = new RadioSetting<>(MarkAction.class)
            .name("Vor Nametag")
            .icon("yellow_name")
            .config("features.trusted_list.display_name")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName);

    private final BooleanSetting showInProfile = new BooleanSetting()
            .name("In /profil anzeigen")
            .config("features.trusted_list.show_in_profile")
            .icon("info")
            .defaultValue(true);

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Trustedliste")
            .config("features.trusted_list.active")
            .icon("green_scroll")
            .defaultValue(false)
            .callback(v -> TabListNameUpdateEvent.updateTabListNames())
            .subSettingsWithHeader("Trustedliste",
                                   tabAction, chatAction, displayNameAction, showInProfile);

    public TrustedList() {
        super("Trusted", ModColor.GREEN, 5, "§a[§lTRUSTED§a] ", "§a[✰] ");
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @Override
    List<PlayerListProvider.PlayerListEntry> getEntries(String name, UUID uuid) {
        List<PlayerListProvider.PlayerListEntry> result = new ArrayList<>();
        for (PlayerListProvider.PlayerListEntry entry : PlayerListProvider.trustedList)
	        if (entry.getName().equalsIgnoreCase(name) || (uuid != null && uuid.equals(entry.getUuid())))
		        result.add(entry);

		return result;
    }

    @Override
    MarkAction getTabAction() {
        return tabAction.get();
    }

    @Override
    MarkAction getChatAction() {
        return chatAction.get();
    }

    @Override
    MarkAction getDisplayNameAction() {
        return displayNameAction.get();
    }

    @Override
    boolean showInProfile() {
        return showInProfile.get();
    }
}