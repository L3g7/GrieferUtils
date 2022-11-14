/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util.misc;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketSendEvent;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * A queue for delaying outgoing chat messages.
 */
public class ChatQueue {

	private static final int QUEUE_DELAY = 50; // 2.5s

	private static final List<String> queuedMessages = new ArrayList<>();
	private static int currentQueueDelay = QUEUE_DELAY;

	@EventListener
	private static void onPacketSend(PacketSendEvent event) {
		if (!(event.packet instanceof C01PacketChatMessage))
			return;

		currentQueueDelay = QUEUE_DELAY;
	}

	@EventListener
	private static void onTick(TickEvent.ClientTickEvent event) {
		currentQueueDelay--;
		if (currentQueueDelay <= 0 && !queuedMessages.isEmpty()) {
			player().sendChatMessage(queuedMessages.remove(0));
			currentQueueDelay = QUEUE_DELAY;
		}
	}

	public static void send(String message) {
		queuedMessages.add(message);
	}

}
