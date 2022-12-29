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

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.util.misc.TickScheduler;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class ChatLineAddEvent extends Event {

	private static final List<String> lines = new ArrayList<>();

	private final String message;

	public ChatLineAddEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public static void onLineAdd(String message) {
		if (lines.isEmpty()) {
			TickScheduler.runAfterRenderTicks(() -> {
				String msg = lines.stream().reduce(String::concat).orElseThrow(() -> new RuntimeException("wtf"));
				msg = msg.replaceAll("§.", "");
				EVENT_BUS.post(new ChatLineAddEvent(msg));

				lines.clear();
			}, 1);
		}

		lines.add(message);
	}

}