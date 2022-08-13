package dev.l3g7.griefer_utils.features.misc;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.labymod.settings.elements.SettingsElement;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
public class SendUsageData extends Feature {

	private final RadioSetting<SendMode> sendUsageData = new RadioSetting<>(SendMode.class)
			.name("§fNutzungsdaten senden")
			.icon("info")
			.defaultValue(SendMode.NORMAL)
			.description("Würde uns freuen, wenn es 'Normal' bleibt ^^\n\nIm normalen Modus wird nur die Account-UUID mitgesendet, im anonymen nichts.")
			.config("send_usage_data")
			.stringProvider(SendMode::getName);

	public SendUsageData() {
		super(Category.MISC);
	}

	@Override
	public SettingsElement getMainElement() {
		return sendUsageData;
	}

	private enum SendMode {

		NORMAL("Normal"), ANONYMOUS("Anonym");

		private final String name;

		SendMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@LateInit
	public void sendUsageData() {
		if (VersionUtil.getAddonVersion().equals("DEBUG"))
			return;

		if (sendUsageData.get() == SendMode.ANONYMOUS) {
			IOUtil.request("https://grieferutils.l3g7.dev/analytics/").close();
			return;
		}

		JsonObject data = new JsonObject();

		UUID uuid = mc().getSession().getProfile().getId();
		data.addProperty("uuid_most", uuid.getMostSignificantBits());
		data.addProperty("uuid_least", uuid.getLeastSignificantBits());

		IOUtil  .request("https://grieferutils.l3g7.dev/analytics/")
				.post("application/json", data.toString().getBytes(UTF_8))
				.close();
	}

}
