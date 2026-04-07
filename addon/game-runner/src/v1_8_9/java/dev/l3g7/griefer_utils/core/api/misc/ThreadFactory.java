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
	private final Integer priority;
	private final AtomicLong count = new AtomicLong(0);

	public ThreadFactory(String nameFormat, int priority) {
		this.nameFormat = nameFormat;
		this.priority = priority;
	}

	@Override
	public Thread newThread(@NotNull Runnable r) {
		String name = nameFormat.contains("%d")
			? String.format(nameFormat, count.getAndIncrement())
			: nameFormat;

		return create(name, priority, r);
	}

	public static void run(String name, int priority, Runnable r) {
		create(name, priority, r).start();
	}

	public static void addShutdownHook(String name, int priority, Runnable r) {
		Runtime.getRuntime().addShutdownHook(create(name, priority, r));
	}

	private static Thread create(String name, int priority, Runnable r) {
		Thread t = new Thread(r, name);
		t.setPriority(priority);
		return t;
	}

}
