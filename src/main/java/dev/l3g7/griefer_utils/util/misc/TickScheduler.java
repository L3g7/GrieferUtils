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
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scheduler for delaying code while staying synchronized with Minecraft's client ticks.
 */
public class TickScheduler {

    private static final Map<Runnable, Integer> clientTickTasks = new HashMap<>();

	/**
	 * Runs the given runnable when the next client tick is triggered.
	 */
    public static void runNextTick(Runnable runnable) {
        runLater(runnable, 1);
    }

	/**
	 * Runs the given runnable after the given delay in client ticks.
	 */
    public static void runLater(Runnable runnable, int delay) {
        clientTickTasks.put(runnable, delay);
    }

    @EventListener
    private static void onClientTick(TickEvent.ClientTickEvent event) {
        List<Runnable> runnableList = new ArrayList<>(clientTickTasks.keySet());
        for (Runnable runnable : runnableList) {
			// Decrease time, run if 0
            if (clientTickTasks.compute(runnable, (r, i) -> i - 1) == 0) {
                clientTickTasks.remove(runnable);
                runnable.run();
            }
        }
    }

}
