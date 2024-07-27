/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class ConcurrentHashMapIterator<K, V> implements Iterator<Entry<K, V>> {

	private static final Function<HashMap<?, ?>, Entry<?, ?>[]> getTable;
	private static final Function<HashMap<?, ?>, Integer> getModCount;

	static {
		try {
			if (LABY_4.isActive()) {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				Unsafe unsafe = (Unsafe) theUnsafe.get(null);
				unsafe.putInt(lookup, 12, -1);

				Class<?> nodeArrayClass = lookup.findClass("[Ljava.util.HashMap$Node;");
				MethodHandle tableGetter = lookup.findGetter(HashMap.class, "table", nodeArrayClass);
				getTable = map -> c(tableGetter.invoke(map));
				MethodHandle modCountGetter = lookup.findGetter(HashMap.class, "modCount", int.class);
				getModCount = map -> (int) modCountGetter.invoke(map);
			} else {
				getTable = map -> Reflection.get(map, "table");
				getModCount = map -> Reflection.get(map, "modCount");
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private final HashMap<K, V> map;
	private final int modCount;
	private final Entry<K, V>[] table;
	private int index = -1;

	public ConcurrentHashMapIterator(HashMap<K, V> map) {
		this.map = map;
		this.modCount = getModCount.apply(map);
		this.table = c(getTable.apply(map));
	}

	public Iterable<Entry<K, V>> toIterator() {
		return () -> this;
	}

	@Override
	public boolean hasNext() {
		do {
			index++;
		} while(index < table.length && table[index] == null);
 		return index != table.length;
	}

	@Override
	public Entry<K, V> next() {
		return table[index];
	}

	public boolean isUpToDate() {
		return modCount == getModCount.apply(map);
	}

}
