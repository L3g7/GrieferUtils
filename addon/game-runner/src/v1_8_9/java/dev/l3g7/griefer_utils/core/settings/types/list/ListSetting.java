/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerListEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.l3g7.griefer_utils.core.settings.Settings.settings;

public interface ListSetting<E extends ListEntry<E>> extends AbstractSetting<ListSetting<E>, List<E>> {

	static <E extends ListEntry<E>> ListSetting<E> create(Class<E> type) {return settings.createListSetting(type);}

	static ListSetting<PlayerListEntry> createPlayerList() {return settings.createPlayerListSetting();}

	static ListSetting<StringListEntry> createStringList() {return settings.createStringListSetting();}

	/**
	 * Adds a value to the list without calling {@link #notifyChange()}.
	 */
	void add(E value);

	/**
	 * Registers a callback for entry edits and adds.
	 */
	ListSetting<E> customEdit(Consumer<@Nullable E> callback);

	/**
	 * Unpacks the container holding the entries.
	 */
	ListSetting<E> unpacked();

}
