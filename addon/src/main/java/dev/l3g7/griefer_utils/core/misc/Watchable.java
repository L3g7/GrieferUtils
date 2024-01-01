/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.misc.functions.Runnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Watchable<T> {

	private volatile T value = null;
	private transient final List<Consumer<T>> listeners = Collections.synchronizedList(new ArrayList<>());

	public T get() {
		return value;
	}

	public void set(T value) {
		if (value == null)
			return;

		this.value = value;
		for (Consumer<T> listener : listeners)
			listener.accept(value);
		listeners.clear();
	}

	public void whenSet(Consumer<T> cb) {
		if (isSet())
			cb.accept(value);
		else
			listeners.add(cb);
	}

	public void whenSet(Runnable cb) {
		whenSet(v -> cb.run());
	}

	public void blockUntilSet() {
		CompletableFuture<Void> cb = new CompletableFuture<>();
		whenSet(() -> cb.complete(null));
		cb.join();
	}

	public boolean isSet() {
		return value != null;
	}

}