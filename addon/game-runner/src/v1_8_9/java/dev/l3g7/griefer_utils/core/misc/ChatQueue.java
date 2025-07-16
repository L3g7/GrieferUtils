/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * A queue for delaying outgoing chat messages.
 */
@Singleton
public class ChatQueue {

	private static final int QUEUE_DELAY = 50; // 2.5s

	public static final List<String> queuedSlowMessages = new ArrayList<>(); // NOTE: refactor
	private static final List<String> queuedMessages = new ArrayList<>();
	private static final List<Triple<String, Future<Void>, Runnable>> blockingMessages = new ArrayList<>();
	private static int currentQueueDelay = QUEUE_DELAY;
	private static long lastMessageSentTimestamp = 0; // When the last message was sent. Used for block timeout
	private static Pair<Future<Void>, Runnable> currentBlock = null;

	@EventListener
	public void onPacketSend(PacketSendEvent<C01PacketChatMessage> event) {
		if (blockingMessages.isEmpty())
			currentQueueDelay = Math.max(QUEUE_DELAY, currentQueueDelay);
	}

	@EventListener
	public void onQuit(ServerEvent.ServerQuitEvent event) {
		lastMessageSentTimestamp = 0;
		currentBlock = null;
		queuedMessages.clear();
		queuedSlowMessages.clear();
		blockingMessages.clear();
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {

		currentQueueDelay--;

		if (currentBlock != null) {
			if (currentBlock.getLeft().isDone())
				currentBlock = null;
			else {
				// Drop block if it's taking longer than 2.5s
				if ((System.currentTimeMillis() - lastMessageSentTimestamp) >= 3000) {
					currentBlock.getRight().run();
					currentBlock = null;
				}

				// Block queue
				return;
			}
		}

		// Process messages
		if (currentQueueDelay <= 0 && (!queuedMessages.isEmpty() || !queuedSlowMessages.isEmpty() || !blockingMessages.isEmpty()) && player() != null) {
			String msg;
			if (!blockingMessages.isEmpty()) { // Prioritize blocking messages
				Triple<String, Future<Void>, Runnable> entry = blockingMessages.remove(0);
				msg = entry.getLeft();
				currentBlock = Pair.of(entry.getMiddle(), entry.getRight());
				currentQueueDelay = QUEUE_DELAY;
			} else {
				if (!queuedMessages.isEmpty()) {
					msg = queuedMessages.remove(0);
					currentQueueDelay = QUEUE_DELAY;
				} else {
					msg = queuedSlowMessages.remove(0);
					currentQueueDelay = 120;
				}
			}

			player().sendChatMessage(msg);
			lastMessageSentTimestamp = System.currentTimeMillis();
		}
	}

	public static void send(String message) {
		queuedMessages.add(message);
	}

	public static void remove(String message) {
		queuedMessages.remove(message);
	}

	/**
	 * Sends a message and blocks the queue until the future is completed.
	 */
	public static CompletableFuture<Void> sendBlocking(String message, Runnable errorMessage) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		blockingMessages.add(Triple.of(message, future, errorMessage));
		return future;
	}

}
