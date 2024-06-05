/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.event.event_bus;

import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;

import java.util.Iterator;

class Listener {

	final Object owner;
	final int priority;
	final Consumer<Event> consumer;

	Listener(Object owner, int priority, Consumer<Event> consumer) {
		this.owner = owner;
		this.priority = priority;
		this.consumer = consumer;
	}

	static class ListenerList implements Iterable<Listener> {

		private volatile Node firstNode = null;

		/**
		 * The last node in each priority, for quick insertion.
		 */
		private final Node[] lastNodes = new Node[5];

		public void add(Listener listener) {
			Node newNode = new Node(listener);

			// The new listener is the first to be added to the list
			if (firstNode == null) {
				firstNode = newNode;
				lastNodes[listener.priority] = newNode;
				return;
			}

			// Try to insert after the node before with the highest priority
			for (int i = listener.priority; i >= 0; i--) {
				if (lastNodes[i] != null) {
					lastNodes[i].insertAfter(newNode);
					lastNodes[listener.priority] = newNode;
					return;
				}
			}

			// All nodes have a lower priority, the new listener is added at the start
			firstNode.insertBefore(newNode);
			if (newNode.previous == null)
				firstNode = newNode;

			lastNodes[listener.priority] = newNode;
		}

		/**
		 * Unregisters all listening methods defined by the given owner.
		 */
		public void removeEventsOf(Object owner) {
			Iterator<Listener> it = iterator();
			while (it.hasNext()) {
				Listener listener = it.next();
				if (listener.owner == owner)
					it.remove();
			}
		}

		public boolean isEmpty() {
			return firstNode == null;
		}

		@Override
		public Iterator<Listener> iterator() {
			return new Iterator<Listener>() {
				public Node node = null;

				@Override
				public boolean hasNext() {
					return (node == null ? firstNode : node.next) != null;
				}

				@Override
				public Listener next() {
					node = node == null ? firstNode : node.next;
					return node.listener;
				}

				public void remove() {
					// Remove current node
					if (node.previous != null)
						node.previous.next = node.next;
					if (node.next != null)
						node.next.previous = node.previous;

					if (firstNode == node)
						firstNode = node.next;

					// Update lastNodes
					int priority = node.listener.priority;
					if (lastNodes[priority] != node)
						return;

					if (node.previous != null && node.previous.listener.priority == priority)
						lastNodes[priority] = node.previous;
					else
						lastNodes[priority] = null;
				}
			};
		}

		private static class Node {

			public Node(Listener listener) {
				this.listener = listener;
			}

			private final Listener listener;
			private volatile Node previous = null;
			private volatile Node next = null;

			private void insertBefore(Node node) {
				node.next = this;
				node.previous = previous;
				if (previous != null)
					previous.next = node;
				previous = node;
			}

			private void insertAfter(Node node) {
				node.previous = this;
				node.next = next;
				if (next != null)
					next.previous = node;
				next = node;
			}

		}

	}
}
