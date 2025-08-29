/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.commands;

import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Command {

	private static final int GREEDY = Integer.MIN_VALUE;

	public final String base;
	private final Argument<?>[] arguments;
	private final Consumer<Arguments> predicate;

	private Command(String base, Argument<?>[] arguments, Consumer<Arguments> predicate) {
		this.base = base;
		this.arguments = arguments;
		this.predicate = predicate;
	}

	/**
	 * @return whether the command succeeded.
	 */
	public boolean process(Queue<String> input) throws Throwable {
		if (input.size() == 0 && arguments.length != 0)
			return false;

		int args = arguments.length;
		if (args > input.size())
			return false;

		Object[] parsedArgs = new Object[args];

		// Parse arguments
		int arg = 0;
		while (!input.isEmpty()) {
			// Unparsed tokens
			if (arg >= args)
				return false;

			Argument<?> argument = arguments[arg];
			// Not enough tokens
			if (argument.size() > input.size())
				return false;

			try {
				parsedArgs[arg] = arguments[arg++].parser().apply(input);
			} catch (Throwable t) {
				return false;
			}
		}

		predicate.acceptWithThrowable(new Arguments(this, parsedArgs));
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(base);
		for (Argument<?> arg : arguments) {
			boolean appendBrackets = !arg.name().contains("<");

			builder.append(' ');
			if (appendBrackets) builder.append('<');
			builder.append(arg.name);
			if (appendBrackets) builder.append('>');
		}

		return builder.toString();
	}

	private record Argument<T>(String name, int size, Class<T> type, Function<Queue<String>, T> parser) {}

	public static class Arguments {
		private final Command command;
		private final Object[] input;

		private Arguments(Command command, Object[] input) {
			this.command = command;
			this.input = input;
		}

		public <T> T get(String name) {
			for (int i = 0; i < command.arguments.length; i++) {
				Argument<?> argument = command.arguments[i];
				if (argument.name.equalsIgnoreCase(name))
					return (T) argument.type.cast(input[i]);
			}

			throw new IllegalArgumentException("Argument \"" + name + "\" does not exist in the command \"" + command.base + "\".");
		}
	}

	public static class CommandBuilder {

		private final String base;
		private final List<Argument<?>> arguments = new ArrayList<>();

		private CommandBuilder(String base) {
			this.base = base;
		}

		public static CommandBuilder command(String base) {
			return new CommandBuilder(base);
		}

		public CommandBuilder stringArg(String name) {
			arguments.add(new Argument<>(name, 1, String.class, Queue::remove));
			return this;
		}

		public CommandBuilder longArg(String name) {
			arguments.add(new Argument<>(name, 1, Long.class, q -> Long.parseLong(q.remove())));
			return this;
		}

		public CommandBuilder doubleArg(String name) {
			arguments.add(new Argument<>(name, 1, Double.class, q -> Double.parseDouble(q.remove())));
			return this;
		}

		public CommandBuilder greedyString(String name) {
			arguments.add(new Argument<>(name, GREEDY, String.class, q -> {
				StringBuilder sb = new StringBuilder();
				while (!q.isEmpty()) {
					if (!sb.isEmpty())
						sb.append(' ');
					sb.append(q.remove());
				}

				return sb.toString();
			}));
			return this;
		}

		public Command build(Consumer<Arguments> predicate) {
			return new Command(base, arguments.toArray(Argument[]::new), predicate);
		}

	}

}