package dev.l3g7.griefer_utils.features.tweaks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.user.User;
import net.labymod.utils.ModColor;

import java.util.UUID;

@Singleton
public class ClanTags extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.config("tweaks.clan_tags.active")
		.icon("rainbow_name")
		.defaultValue(true);

	public ClanTags() {
		super(Category.TWEAK);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMMCustomPayload(MMCustomPayloadEvent event) {
		if (!event.getChannel().equals("user_subtitle"))
			return;

		for (JsonElement elem : event.getPayload().getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());
			User user = LabyMod.getInstance().getUserManager().getUser(uuid);

			user.setSubTitle(ModColor.createColors(obj.get("text").getAsString()));
			user.setSubTitleSize(0.5);
		}
	}

}
