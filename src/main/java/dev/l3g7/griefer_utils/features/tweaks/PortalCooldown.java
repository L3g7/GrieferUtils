package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.server.ServerQuitEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class PortalCooldown extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Portal-Cooldown")
		.description("Zeigt dir den 12s-Cooldown beim Betreten des Portalraums in der XP-Leiste an.")
		.config("tweaks.portal_cooldown.active")
		.icon("hourglass")
		.defaultValue(true);

    public PortalCooldown() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

	private long timeoutEnd = 0;

	@SubscribeEvent
	public void onMessage(ClientChatReceivedEvent event) {
		if (event.message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§fDu bist im §r§5Portalraum§r§f. Wähle deinen Citybuild aus.§r")) {
			timeoutEnd = System.currentTimeMillis() + 12 * 1000;
		}
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent event) {
		timeoutEnd = 0;
	}

	@EventListener
	public void onServerQuit(ServerQuitEvent event) {
		timeoutEnd = 0;
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isActive() || player() == null)
			return;

		if (timeoutEnd != 0) {
			int delta = (int) (timeoutEnd - System.currentTimeMillis());
			player().experienceLevel = (int) Math.ceil(delta / 1000f);
			player().experience = delta / 12000f;
		}
	}

}
