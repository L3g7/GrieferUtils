package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketSendEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerQuitEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * ugly af spaghetti code
 */
@Singleton
public class ChatQueue {

	private static final int QUEUE_DELAY = 50; // 2.5s

	private static final List<String> queuedMessages = new ArrayList<>();
	private static final List<Triple<String, Future<Void>, String>> blockingMessages = new ArrayList<>();
	private static int currentQueueDelay = QUEUE_DELAY;
	private static long lastMessageSentTimestamp = 0; // When the last message was sent. Used for block timeout
	private static int messagesSentWithoutDelay = 0; // Counter how many messages were sent without chat delay. If >=3, a 60t delay will be forced
	private static Pair<Future<Void>, String> currentBlock = null;

	@EventListener
	public void onPacketSend(PacketSendEvent event) {
		if (!(event.getPacket() instanceof C01PacketChatMessage))
			return;

		if (blockingMessages.isEmpty()) {
			messagesSentWithoutDelay = 0;
			currentQueueDelay = QUEUE_DELAY;
		}
	}

	@EventListener
	public void onQuit(ServerQuitEvent event) {
		lastMessageSentTimestamp = messagesSentWithoutDelay = 0;
		currentBlock = null;
		queuedMessages.clear();
		blockingMessages.clear();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START)
			return;

		currentQueueDelay--;

		if (currentBlock != null) {
			if (currentBlock.a.isDone())
				currentBlock = null;
			else {
				// Show title
				Minecraft.getMinecraft().ingameGUI.displayTitle("", null, -1, -1, -1);
				Minecraft.getMinecraft().ingameGUI.displayTitle(null, "§eGrieferUtils wird initialisiert...", -1, -1, -1);
				Minecraft.getMinecraft().ingameGUI.displayTitle(null, null, 0, 2, 3);

				// Block motion
				Entity e = Minecraft.getMinecraft().thePlayer;
				e.motionX = e.motionY = e.motionZ = 0;
				e.onGround = true;

				if ((System.currentTimeMillis() - lastMessageSentTimestamp) >= 3000) {
					Feature.displayAchievement("§c§lFehler \u26A0", "§c" + currentBlock.b);
					currentBlock = null; // Drop block if it's taking longer than 2.5s
				}
				return;
			}
		}

		// Process messages
		if (currentQueueDelay <= 0 && (!queuedMessages.isEmpty() || !blockingMessages.isEmpty())) {
			String msg;
			if (!blockingMessages.isEmpty()) { // Prioritize blocking messages
				++messagesSentWithoutDelay;
				Triple<String, Future<Void>, String> entry = blockingMessages.remove(0);
				msg = entry.getLeft();
				currentBlock = Pair.of(entry.getMiddle(), entry.getRight());
			} else {
				msg = queuedMessages.remove(0);
				messagesSentWithoutDelay = 0;
				currentQueueDelay = QUEUE_DELAY;
			}

			if (EventBus.post(new MessageSendEvent(msg)).isCanceled())
				return;

			Minecraft.getMinecraft().thePlayer.sendChatMessage(msg);
			lastMessageSentTimestamp = System.currentTimeMillis();

			// Force 60t delay if the last 3 messages were without delay
			if (messagesSentWithoutDelay >= 3) {
				messagesSentWithoutDelay = 0;
				currentQueueDelay = 60;
			}
		}
	}

	public static void send(String message) {
		queuedMessages.add(message);
	}

	/**
	 * Sends a message and blocks movement until the future is completed.
	 */
	public static CompletableFuture<Void> sendBlocking(String message, String errorMessage) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		blockingMessages.add(Triple.of(message, future, errorMessage));
		return future;
	}

}
