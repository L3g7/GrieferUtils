/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

/**
 * A queue for delaying outgoing chat messages.
 */
@Singleton
public class ChatQueue {

	private static final int QUEUE_DELAY = 50; // 2.5s

	private static final List<String> queuedMessages = new ArrayList<>();
	private static final List<Triple<String, Future<Void>, Runnable>> blockingMessages = new ArrayList<>();
	private static int currentQueueDelay = QUEUE_DELAY;
	private static long lastMessageSentTimestamp = 0; // When the last message was sent. Used for block timeout
	private static int messagesSentWithoutDelay = 0; // Counter how many messages were sent without chat delay. If >=3, a 60t delay will be forced
	private static Pair<Future<Void>, Runnable> currentBlock = null;

	@EventListener
	public void onPacketSend(PacketSendEvent<C01PacketChatMessage> event) {
		if (blockingMessages.isEmpty()) {
			messagesSentWithoutDelay = 0;
			currentQueueDelay = QUEUE_DELAY;
		}
	}

	@EventListener
	public void onQuit(ServerEvent.ServerQuitEvent event) {
		lastMessageSentTimestamp = messagesSentWithoutDelay = 0;
		currentBlock = null;
		queuedMessages.clear();
		blockingMessages.clear();
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {

		currentQueueDelay--;

		if (currentBlock != null) {
			if (currentBlock.getLeft().isDone())
				currentBlock = null;
			else {
				// Show title
				Minecraft.getMinecraft().ingameGUI.displayTitle("", null, -1, -1, -1);
				Minecraft.getMinecraft().ingameGUI.displayTitle(null, "§eGrieferUtils wird initialisiert...", -1, -1, -1);
				Minecraft.getMinecraft().ingameGUI.displayTitle(null, null, 0, 2, 3);

				// Block motion
				Entity e = Minecraft.getMinecraft().thePlayer;
				if (e == null)
					return;

				e.motionX = e.motionY = e.motionZ = 0;
				e.onGround = true;

				if ((System.currentTimeMillis() - lastMessageSentTimestamp) >= 3000) {
					currentBlock.getRight().run();
					currentBlock = null; // Drop block if it's taking longer than 2.5s
				}
				return;
			}
		}

		// Process messages
		if (currentQueueDelay <= 0 && (!queuedMessages.isEmpty() || !blockingMessages.isEmpty()) && player() != null) {
			String msg;
			if (!blockingMessages.isEmpty()) { // Prioritize blocking messages
				++messagesSentWithoutDelay;
				Triple<String, Future<Void>, Runnable> entry = blockingMessages.remove(0);
				msg = entry.getLeft();
				currentBlock = Pair.of(entry.getMiddle(), entry.getRight());
			} else {
				msg = queuedMessages.remove(0);
				messagesSentWithoutDelay = 0;
				currentQueueDelay = QUEUE_DELAY;
			}

			player().sendChatMessage(msg);
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

	public static void remove(String message) {
		queuedMessages.remove(message);
	}

	/**
	 * Sends a message and blocks movement until the future is completed.
	 */
	public static CompletableFuture<Void> sendBlocking(String message, Runnable errorMessage) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		blockingMessages.add(Triple.of(message, future, errorMessage));
		return future;
	}

}
