package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

@Singleton
public class AutoPortal extends Feature {

	private final BooleanSetting join = new BooleanSetting()
			.name("Automatisch beitreten")
			.description("Betritt automatisch den Portalraum, sobald Minecraft gestartet wurde.")
			.icon(Material.GRASS)
			.defaultValue(false)
			.config("features.auto_portal.join");

	private final BooleanSetting enabled = new BooleanSetting()
			.name("AutoPortal")
			.description("Geht automatisch in den Portalraum.")
			.icon("portal")
			.defaultValue(true)
			.config("features.auto_portal.active")
			.subSettingsWithHeader("AutoPortal", join);

	public AutoPortal() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onJoin(ServerJoinEvent event) {
		if (isActive() && isOnGrieferGames())
			send("/portal");
	}

	@LateInit
	public void onInit() {
		if (isActive() && join.get())
			mc().displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc(), new ServerData("GrieferGames", "griefergames.net", false)));
	}
}
