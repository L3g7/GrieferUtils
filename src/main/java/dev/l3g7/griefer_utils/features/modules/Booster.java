package dev.l3g7.griefer_utils.features.modules;


import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class Booster extends Module {

	private final List<BoosterData> booster = new ArrayList<>();

	private final BooleanSetting shorten = new BooleanSetting()
			.name("Zeit kürzen")
			.icon(Material.LEVER)
			.config("modules.booster.shorten")
			.defaultValue(false);

	public Booster() {
		super("Booster", "Zeigt dir die momentan aktiven Booster an", "booster", new IconData(Material.FIREWORK));
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent event) {
		booster.clear();
	}

	@EventListener
	public void onMMCustomPayload(MMCustomPayloadEvent event) {
		if(!event.getChannel().equals("booster"))
			return;

		JsonObject data = event.getPayload().getAsJsonObject();

		System.out.println("[GrieferUtils DEBUG] " + data.toString());

		// Get BoosterData
		String name = data.get("name").getAsString();
		BoosterData boosterData = booster.stream().filter(b -> b.name.equals(name)).findAny().orElse(null);
		if(boosterData == null)
			booster.add(boosterData = new BoosterData(name));

		// Load data from payload
		boosterData.stackable = data.get("stackable").getAsBoolean();
		if(!boosterData.stackable)
			boosterData.expirationDates.clear();
		boosterData.expirationDates.add(data.get("runUntil").getAsLong() + System.currentTimeMillis());
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(shorten);
	}

	@Override
	public String[] getKeys() {
		booster.removeIf(BoosterData::isExpired);

		if (booster.isEmpty())
			return new String[]{"Booster"};

		// Get names (and counts) as Strings
		List<String> boosterKeys = booster.stream()
				.map(b -> b.stackable ? b.count() + "x " + b.name : b.name)
				.collect(Collectors.toList());

		// Add "Booster" to start
		boosterKeys.add(0, "Booster");

		return boosterKeys.toArray(new String[0]);
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0"};
	}

	@Override
	public String[] getValues() {
		if(booster.isEmpty())
			return getDefaultValues();

		// Get expirations dates as Strings
		List<String> boosterValues = booster.stream()
				.map(b -> RenderUtil.formatTime(b.expirationDates.stream().mapToLong(Long::longValue).min().orElse(0), shorten.get()))
				.collect(Collectors.toList());

		// Add count to start
		boosterValues.add(0, String.valueOf(booster.stream().map(BoosterData::count).mapToInt(Integer::intValue).sum()));

		return boosterValues.toArray(new String[0]);
	}

	private static class BoosterData {

		private final String name;
		private final List<Long> expirationDates = new ArrayList<>();
		private boolean stackable;

		public BoosterData(String name) {
			this.name = name;
		}

		private boolean isExpired() {
			long currentTime = System.currentTimeMillis();
			expirationDates.removeIf(d -> d < currentTime);
			return expirationDates.isEmpty();
		}

		private int count() {
			return expirationDates.size();
		}

	}
}
