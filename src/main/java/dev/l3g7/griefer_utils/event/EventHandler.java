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

package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.event.events.RenderWorldEvent;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Handles the logic for forge and annotation based events.
 */
@Singleton
public class EventHandler implements MinecraftUtil {

	private final EventBus EVENT_BUS = MinecraftForge.EVENT_BUS;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(FileProvider.getSingleton(EventHandler.class));
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (mc().currentScreen == null) {
			System.out.println("Posting something, idkm9");
			EVENT_BUS.post(new RenderWorldEvent(event.phase, event.renderTickTime));
		}
	}

}
