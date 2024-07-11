/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ConcurrentHashMapIterator<K, V> implements Iterator<Entry<K, V>> {

	public static <K, V> Iterable<Entry<K, V>> iterate(HashMap<K, V> map) {
		return () -> new ConcurrentHashMapIterator<>(map);
	}

	private final Entry<K, V> firstNode;
	private Entry<K, V> currentNode;
	private Entry<K, V> nextNode;

	public ConcurrentHashMapIterator(HashMap<K, V> map) {
		Entry<K, V>[] table = Reflection.get(map, "table");
		firstNode = table.length == 0 ? null : table[0];
	}

	@Override
	public boolean hasNext() {
		if (firstNode == null)
			return false;

		getNext();
		return nextNode != null;
	}

	@Override
	public Entry<K, V> next() {
		return currentNode = nextNode;
	}

	private void getNext() {
		Entry<K, V> node = currentNode == null ? firstNode : currentNode;
		nextNode = Reflection.get(node, "next");
	}

}
