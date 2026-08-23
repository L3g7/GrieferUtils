/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadFactory implements java.util.concurrent.ThreadFactory {

	private final String nameFormat;
	private final int priority;
	private final boolean daemon;
	private final AtomicLong count = new AtomicLong(0);

	public ThreadFactory(String nameFormat, int priority) {
		this(nameFormat, priority, true);
	}

	public ThreadFactory(String nameFormat, int priority, boolean daemon) {
		this.nameFormat = nameFormat;
		this.priority = priority;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(@NotNull Runnable r) {
		String name = nameFormat.contains("%d")
			? String.format(nameFormat, count.getAndIncrement())
			: nameFormat;

		return create(name, priority, daemon, r);
	}

	/**
	 * Spawns the given runnable as a daemon thread.
	 */
	public static void run(String name, int priority, Runnable r) {
		create(name, priority, true, r).start();
	}

	/**
	 * Spawns the given runnable as a non-daemon thread.
	 */
	public static void runSync(String name, int priority, Runnable r) {
		create(name, priority, false, r).start();
	}

	public static void addShutdownHook(String name, int priority, Runnable r) {
		Runtime.getRuntime().addShutdownHook(create(name, priority, false, r));
	}

	private static Thread create(String name, int priority, boolean daemon, Runnable r) {
		Thread t = new Thread(r, name);
		t.setPriority(priority);
		t.setDaemon(daemon);
		return t;
	}

}
