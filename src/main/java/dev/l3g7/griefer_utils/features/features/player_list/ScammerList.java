package dev.l3g7.griefer_utils.features.features.player_list;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.settings.elements.playerlist.PlayerListSetting;
import dev.l3g7.griefer_utils.settings.elements.playerlist.PlayerSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class ScammerList extends PlayerList {

    private final RadioSetting<MarkAction> tabAction = new RadioSetting<>(MarkAction.class)
            .name("in Tabliste")
            .icon("tab_list")
            .config("features.scammer_list.tab")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName)
            .callback(a -> TabListNameUpdateEvent.updateTabListNames());

    private final RadioSetting<MarkAction> chatAction = new RadioSetting<>(MarkAction.class)
            .name("in Chat")
            .icon("speech_bubble")
            .config("features.scammer_list.chat")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName);

    private final RadioSetting<MarkAction> displayNameAction = new RadioSetting<>(MarkAction.class)
            .name("Vor Nametag")
            .icon("yellow_name")
            .config("features.scammer_list.display_name")
            .defaultValue(MarkAction.ICON)
            .stringProvider(MarkAction::getName);

    private final BooleanSetting showInProfile = new BooleanSetting()
            .name("In /profil anzeigen")
            .config("features.scammer_list.show_in_profile")
            .icon("info")
            .defaultValue(true);

	private final PlayerListSetting customScammerList = new PlayerListSetting()
			.name("%s. Scammer")
			.config("features.scammer_list.custom_entries");

	private final CategorySetting customScammers = new CategorySetting()
		.name("Scammer hinzufügen")
		.icon("red_scroll")
		.subSettingsWithHeader("Scammer", customScammerList);

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Scammerliste")
            .config("features.scammer_list.active")
            .icon("red_scroll")
            .defaultValue(false)
            .callback(v -> TabListNameUpdateEvent.updateTabListNames())
            .subSettingsWithHeader("Scammerliste",
                    tabAction, chatAction, displayNameAction, showInProfile, customScammers);

    public ScammerList() {
        super("Scammer!", ModColor.RED, 14, "§c[§lSCAMMER§c] ", "§c[⚠] ");

		// Load from ScammerRadar's list
	    File scammerFile = new File(mc().mcDataDir, "LabyMod/antiScammer/localScammer.json");
	    if(scammerFile.exists() && scammerFile.length() != 0) {
		    IOUtil.file(scammerFile).readJsonArray(obj -> {
			    for (JsonElement element : obj)
				    customScammerList.add(PlayerSetting.fromJson(element));
		    });
	    }
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @Override
    List<PlayerListProvider.PlayerListEntry> getEntries(String name, UUID uuid) {
        List<PlayerListProvider.PlayerListEntry> result = new ArrayList<>();

		List<PlayerListProvider.PlayerListEntry> entries = new ArrayList<>(PlayerListProvider.scammerList);
		entries.addAll(customScammerList.getEntries());

	    for (PlayerListProvider.PlayerListEntry entry : entries)
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