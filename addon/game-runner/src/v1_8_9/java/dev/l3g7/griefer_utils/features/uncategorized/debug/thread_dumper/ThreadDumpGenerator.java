/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.thread_dumper;

import java.util.*;

public class ThreadDumpGenerator {

	public static String generateThreadDumps(boolean onlyClient) {
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

		if (onlyClient) {
			for (Map.Entry<Thread, StackTraceElement[]> threadEntry : map.entrySet())
				if (threadEntry.getKey().getName().equals("Client thread"))
					return generateThreadDump(threadEntry.getKey(), threadEntry.getValue());

			throw new IllegalStateException("Client thread could not be found");
		}

		TreeMap<Thread, StackTraceElement[]> sorted = new TreeMap<>((t1, t2) -> {
			int priorityCompare = Integer.compare(t1.getPriority(), t2.getPriority());
			if (priorityCompare != 0)
				return priorityCompare;

			return t1.getName().compareToIgnoreCase(t2.getName());
		});

		sorted.putAll(map);

		StringBuilder dumps = new StringBuilder();

		for (Map.Entry<Thread, StackTraceElement[]> threadEntry : sorted.entrySet())
			dumps.append(generateThreadDump(threadEntry.getKey(), threadEntry.getValue()));

		return dumps.toString();
	}

	public static String generateThreadDump(Thread thread, StackTraceElement[] elements) {
		ThreadType type = determineType(thread, elements);
		boolean isThreadClass = thread.getClass() == Thread.class || type == ThreadType.WAITING_TIMER;

		StringBuilder dump = new StringBuilder(String.format("[%d] %s%s\n", thread.getPriority(), type, generateName(isThreadClass, thread)));

		if (type == ThreadType.DEAD || type == ThreadType.WAITING_TIMER)
			return dump.toString();

		for (StackTraceElement stackTraceElement : filterStackTrace(elements, type, isThreadClass)) {
			dump.append("\tat ")
				.append(stackTraceElement)
				.append("\n");
		}

		return dump.toString();
	}

	private static String generateName(boolean isThreadClass, Thread thread) {
		boolean isUnnamed = thread.getName().startsWith("Thread-");
		String className = String.format("{%s}", thread.getClass().getName());

		StringBuilder name = new StringBuilder();

		if (!isUnnamed || isThreadClass)
			name.append(thread.getName());

		if (!isThreadClass) {
			if (!isUnnamed)
				name.append(" | ");
			name.append(className);
		}

		return name.toString();
	}

	private static ThreadType determineType(Thread thread, StackTraceElement[] stackTraceElements) {
		if (stackTraceElements.length == 0)
			return ThreadType.DEAD;

		StackTraceElement currentStackTrace = stackTraceElements[0];
		String clazz = currentStackTrace.getClassName();
		String method = currentStackTrace.getMethodName();

		if (clazz.equals("sun.misc.Unsafe") && method.equals("park"))
			return ThreadType.PARKED;

		if (clazz.equals("java.lang.Object") && method.equals("wait")) {
			if (thread.getClass().getName().equals("java.util.TimerThread"))
				return ThreadType.WAITING_TIMER;

			return ThreadType.WAITING;
		}

		if (clazz.equals("java.lang.Thread") && method.equals("sleep"))
			return ThreadType.SLEEPING;

		return ThreadType.NORMAL;
	}

	private static List<StackTraceElement> filterStackTrace(StackTraceElement[] elements, ThreadType type, boolean isThreadClass) {
		List<StackTraceElement> list = new ArrayList<>(Arrays.asList(elements));
		if (type != ThreadType.NORMAL)
			list.remove(0);

		if (isThreadClass)
			list.remove(list.size() - 1);

		list.removeIf(e -> {
			String cl = e.getClassName();
			String method = e.getMethodName();

			return cl.startsWith("java.util.concurrent.locks.") || cl.equals("java.util.concurrent.ArrayBlockingQueue");
		});

		return list;
	}

	private enum ThreadType {
		NORMAL,
		DEAD,

		PARKED,
		WAITING,
		WAITING_TIMER,
		SLEEPING;

		@Override
		public String toString() {
			if (this == NORMAL)
				return "";

			return '[' + name() + "] ";
		}
	}

}
