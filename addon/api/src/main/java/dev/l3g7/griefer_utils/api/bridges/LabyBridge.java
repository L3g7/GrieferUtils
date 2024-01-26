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

package dev.l3g7.griefer_utils.api.bridges;

import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.misc.Pair;
import dev.l3g7.griefer_utils.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;

import java.util.UUID;
import java.util.function.BiFunction;

@Bridged
public interface LabyBridge {

	LabyBridge labyBridge = FileProvider.getBridge(LabyBridge.class);

	static void run(Runnable laby3, Runnable laby4) {
		(labyBridge.laby4() ? laby4 : laby3).run();
	}

	// General information

	boolean obfuscated();

	boolean forge();

	boolean laby4();

	String addonVersion();

	float partialTicks();

	// Utility methods

	default void notifyMildError(String message) {labyBridge.notify("§e§lFehler ⚠", "§e" + message);}

	void notify(String title, String message);

	/**
	 * Adds a button redirecting to the support discord server, if supported.
	 */
	void notifyError(String message);

	static void display(String message) {labyBridge.displayInChat(message);}

	static void display(String format, Object... args) {display(String.format(format, args));}

	void displayInChat(String message);

	// Events

	void onJoin(Runnable callback);

	void onQuit(Runnable callback);

	void onAccountSwitch(Runnable callback);

	void onMessageSend(Predicate<String> callback);

	void onMessageModify(BiFunction<Object, Object, Object> callback);

	// Specific methods

	/**
	 * Invokes a LabyMod message send event and returns whether it was canceled.
	 */
	boolean trySendMessage(String message);

	/**
	 * Creates a HeaderSetting acting as a padding for dropdowns.
	 * Only required in LabyMod 3, returns null otherwise.
	 */
	HeaderSetting createLaby3DropDownPadding();

	Pair<String, String> getCachedTexture(UUID uuid);

	void removeChatLine(int id);

	/**
	 * Replaces the last entry in the sent message history.
	 */
	void replaceLastSentMessage(String replacement);

}
