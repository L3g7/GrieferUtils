/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;

import java.util.Timer;
import java.util.TimerTask;

public class DebounceTimer {

	private final Timer timer;
	private Runnable activeRunnable = null;
	private final int debounce;

	public DebounceTimer(String name, int debounce) {
		this.timer = new Timer("GrieferUtils-DebounceTimer-" + name, true);
		this.debounce = debounce;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Runnable runnable;
			synchronized (this) {
				runnable = activeRunnable;
				activeRunnable = null;
			}

			if (runnable != null)
				runnable.run();
		}));
	}

	public void schedule(Runnable runnable) {
		synchronized (this) {
			activeRunnable = runnable;
		}

		try {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					synchronized (this) {
						if (activeRunnable == runnable)
							return;

						activeRunnable = null;
					}
					runnable.run();
				}
			}, debounce);
		} catch (IllegalStateException ignored) {
			// Minecraft is closing and the timer has been killed
		}
	}

}
