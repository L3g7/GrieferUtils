/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
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
		Thread thread = Executors.defaultThreadFactory().newThread(r);

		thread.setName(String.format(nameFormat, count.getAndIncrement()));
		thread.setPriority(priority);

		return thread;
	}

}
