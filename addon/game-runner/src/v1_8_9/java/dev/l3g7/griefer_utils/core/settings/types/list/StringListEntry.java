/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.stream.IntStream;

public class StringListEntry implements CharSequence, ListEntry<StringListEntry> {

	private String text;

	public StringListEntry() {
		this("");
	}

	public StringListEntry(String text) {
		this.text = text;
	}

	@Override
	public String resourceIcon() {
		return "book_and_quill";
	}

	@Override
	public StringListEntry createNew() {
		return new StringListEntry();
	}

	@Override
	public void load(JsonElement data) {
		this.text = data.getAsString();
	}

	public void set(String text) {
		this.text = text;
	}

	@Override
	public JsonElement encode() {
		return new JsonPrimitive(text);
	}

	@Override
	public String getName() {
		return text;
	}

	public String get() {
		return text;
	}

	@Override
	public int length() {
		return text.length();
	}

	@Override
	public char charAt(int index) {
		return text.charAt(index);
	}

	@Override
	public @NotNull CharSequence subSequence(int start, int end) {
		return text.subSequence(start, end);
	}

	@Override
	public boolean isEmpty() {
		return text.isEmpty();
	}

	@Override
	public @NotNull IntStream chars() {
		return text.chars();
	}

	@Override
	public @NotNull IntStream codePoints() {
		return text.codePoints();
	}

	@Override
	public @NonNull String toString() {
		return text;
	}

}
