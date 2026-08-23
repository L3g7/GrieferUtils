/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Runnable;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * A scheduler for delaying code while staying synchronized with Minecraft's client ticks.
 */
public class TickScheduler {

	private static final Map<Runnable, AtomicInteger> clientTickTasks = new HashMap<>();
	private static final Queue<Runnable> renderTickTasks = new ConcurrentLinkedQueue<>();

	/**
	 * Runs the given runnable after one client tick.
	 */
	public static void runNextClientTick(Runnable runnable) {
		runAfterClientTicks(runnable, 1);
	}

	/**
	 * Runs the given runnable after the given delay in client ticks.
	 */
	public static void runAfterClientTicks(Runnable runnable, int delay) {
		if (delay == 0) {
			runnable.run();
			return;
		}

		synchronized (clientTickTasks) {
			clientTickTasks.put(runnable, new AtomicInteger(delay));
		}
	}

	/**
	 * Runs the given runnable after one render tick.
	 */
	public static void runNextRenderTick(Runnable runnable) {
		renderTickTasks.add(runnable);
	}

	/**
	 * Runs the given runnable in the Main thread.
	 */
	public static void sync(Runnable runnable) {
		mc().addScheduledTask(runnable);
	}

	@EventListener
	private static void onClientTick(ClientTickEvent event) {
		synchronized (clientTickTasks) {
			Iterator<Entry<Runnable, AtomicInteger>> it = new HashMap<>(TickScheduler.clientTickTasks).entrySet().iterator();
			while (it.hasNext()) {
				// Decrease time, run if 0
				Entry<Runnable, AtomicInteger> entry = it.next();
				if (entry.getValue().decrementAndGet() == 0) {
					it.remove();
					entry.getKey().run();
				}
			}
		}
	}

	@EventListener
	private static void onRenderTick(RenderTickEvent event) {
		Runnable r;
		while ((r = renderTickTasks.poll()) != null)
			r.run();
	}

}
