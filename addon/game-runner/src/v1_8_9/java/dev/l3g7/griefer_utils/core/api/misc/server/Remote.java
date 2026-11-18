/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;

import java.util.ArrayList;
import java.util.List;

public class Remote<T> {

	private static final List<Remote<?>> PENDING_REMOTES = new ArrayList<>();
	private static StaticDataReceiveEvent STATIC_DATA = null;

	private final String key;

	private T value;

	private final Function<JsonElement, T> parser;
	private final List<Consumer<T>> consumers = new ArrayList<>();

	public Remote(String key, T fallback) {
		this(key, fallback, (Class<T>) fallback.getClass());
	}
	public Remote(String key, Class<T> type) {
		this(key, null, type);
	}

	public Remote(String key, T fallback, Class<T> type) {
		this(key, fallback, json -> IO.GSON.fromJson(json, type));
	}

	public Remote(String key, T fallback, Function<JsonElement, T> parser) {
		this.key = key;
		this.value = fallback;
		this.parser = parser;

		synchronized (PENDING_REMOTES) {
			if (STATIC_DATA != null)
				read(STATIC_DATA);
			else
				PENDING_REMOTES.add(this);
		}
	}

	public Remote<T> addConsumer(Consumer<T> consumer) {
		this.consumers.add(consumer);
		return this;
	}

	/**
	 * @return the contained value, or null if empty.
	 */
	public T get() {
		return value;
	}

	private void read(StaticDataReceiveEvent event) {
		this.value = event.read(key, parser);
		for (Consumer<T> consumer : consumers)
			consumer.accept(this.value);
	}

	@EventListener(priority = Priority.HIGH)
	private static void onStaticDataReceive(StaticDataReceiveEvent event) {
		synchronized (PENDING_REMOTES) {
			STATIC_DATA = event;
			for (Remote<?> pendingRemote : PENDING_REMOTES)
				pendingRemote.read(event);
			PENDING_REMOTES.clear();
		}
	}

}
