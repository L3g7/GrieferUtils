package dev.l3g7.griefer_utils.event.events.griefergames;

import dev.l3g7.griefer_utils.event.EventListener;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * An event being posted after joining a city build.
 */
public class CityBuildJoinEvent extends Event {

	@EventListener
	private static void onMessage(ClientChatReceivedEvent event) {
		if (event.message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDeine Daten wurden vollständig heruntergeladen.§r"))
			MinecraftForge.EVENT_BUS.post(new CityBuildJoinEvent());
	}

}
