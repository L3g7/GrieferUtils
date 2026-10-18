package dev.l3g7.griefer_utils.core.misc.griefer_games;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Re-sends messages caught my GG Auth
 */
public class GGAuthResend {

	private static final String GG_AUTH_SUCCESS = "[GGAuth] Du wurdest erfolgreich verifiziert.";

	private static final List<String> sentMessages = new ArrayList<>();
	private static boolean waitingForGGAuth = false;

	@EventListener
	private static void onServerJoin(GrieferGamesJoinEvent event) {
		waitingForGGAuth = false;
	}

	@EventListener
	private static void onCBJoin(CitybuildJoinEvent.Early event) {
		sentMessages.clear();
		waitingForGGAuth = true;
	}

	@EventListener
	private static void onMessageSent(PacketSendEvent<C01PacketChatMessage> event) {
		sentMessages.add(event.packet.getMessage());
	}

	@EventListener
	private static void onMessageReceive(MessageReceiveEvent event) {
		if (waitingForGGAuth && event.message.getUnformattedText().equals(GG_AUTH_SUCCESS)) {
			waitingForGGAuth = true;
			sentMessages.forEach(ChatQueue::send);
		}
	}

}
