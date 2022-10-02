package dev.l3g7.griefer_utils.features.features;

import com.sun.jna.Platform;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.win32.Winuser;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

@Singleton
public class AutoPortal extends Feature {

	private final BooleanSetting maximize = new BooleanSetting()
			.name("Automatisch maximieren")
			.description("Ob Minecraft nach dem Starten automatisch maximiert werden soll.")
			.icon("maximize")
			.defaultValue(true)
			.config("features.auto_portal.maximize");

	private final BooleanSetting join = new BooleanSetting()
			.name("Start im Portalraum")
			.description("Betritt automatisch Griefergames, sobald Minecraft gestartet wurde.")
			.icon(Material.GRASS)
			.defaultValue(false)
			.config("features.auto_portal.join")
			.subSettingsWithHeader("AutoPortal - Start im Portalraum", maximize);

	private final BooleanSetting enabled = new BooleanSetting()
			.name("AutoPortal")
			.description("Betritt automatisch den Portalraum.")
			.icon("portal")
			.defaultValue(false)
			.config("features.auto_portal.active")
			.subSettingsWithHeader("AutoPortal", join);

	public AutoPortal() {
		super(Category.FEATURE);

		if (Platform.isWindows())
			join.subSettingsWithHeader("AutoPortal - Start im Portalraum", maximize);
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
		if (!isActive() || !join.get())
			return;

		mc().displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc(), new ServerData("GrieferGames", "griefergames.net", false)));

		if (Platform.isWindows() && maximize.get())
			Winuser.maximizeMinecraft();
	}

}
