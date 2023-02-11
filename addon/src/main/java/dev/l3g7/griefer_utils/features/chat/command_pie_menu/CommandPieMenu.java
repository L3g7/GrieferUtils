package dev.l3g7.griefer_utils.features.chat.command_pie_menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class CommandPieMenu extends Feature {

	private String entryKey;

	private final PieMenu pieMenu = new PieMenu();
	private boolean isOpen = false;

	private final BooleanSetting animation = new BooleanSetting()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll")
		.icon("labymod:settings/settings/playermenuanimation")
		.defaultValue(true);

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.triggersInContainers()
		.pressCallback(p -> {
			if (mc().currentScreen != null || !isEnabled() || !ServerCheck.isOnGrieferGames())
				return;

			// Open
			if (p) {
				if (!isOpen) {
					pieMenu.open(animation.get(), getMainElement());
					isOpen = true;
				}
				return;
			}

			// Close
			if (isOpen) {
				pieMenu.close();
				isOpen = false;
			}
		});

	private final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Eintrag hinzufügen")
		.callback(() -> {
			List<SettingsElement> settings = getMainElement().getSubSettings().getElements();
			PieEntryDisplaySetting setting = new PieEntryDisplaySetting("", "", null);
			settings.add(settings.size() - 1, setting);
			setting.openSettings();
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Befehlsradialmenü")
		.description("Ein Radialmenü zum schnellen Ausführen von Citybuild-bezogenen Befehlen.")
		.icon(Material.COMMAND)
		.subSettings(key, animation, newEntrySetting);

	@Override
	public void init() {
		super.init();

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey))
			return;

		JsonArray entries = Config.get(entryKey).getAsJsonArray();
		for (JsonElement entry : entries) {
			JsonObject data = entry.getAsJsonObject();

			ItemStack stack = ItemUtil.CB_ITEMS.get(0);
			for (ItemStack cb : ItemUtil.CB_ITEMS) {
				if (cb.getDisplayName().equals(data.get("cb").getAsString())) {
					stack = cb;
					break;
				}
			}

			PieEntryDisplaySetting pieEntry = new PieEntryDisplaySetting(
				data.get("name").getAsString(),
				data.get("command").getAsString(),
				stack
			);

			List<SettingsElement> settings = enabled.getSubSettings().getElements();
			settings.add(settings.size() - 1, pieEntry);
		}
	}

	public void onChange() {
		mc().currentScreen.initGui();

		JsonArray array = new JsonArray();
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof PieEntryDisplaySetting))
				continue;

			PieEntryDisplaySetting pieEntry = (PieEntryDisplaySetting) element;

			JsonObject entry = new JsonObject();
			entry.addProperty("name", pieEntry.name.get());
			entry.addProperty("command", pieEntry.command.get());
			entry.addProperty("cb", pieEntry.cityBuild.get().getDisplayName());

			array.add(entry);
		}

		Config.set(entryKey, array);
		Config.save();
	}

}
