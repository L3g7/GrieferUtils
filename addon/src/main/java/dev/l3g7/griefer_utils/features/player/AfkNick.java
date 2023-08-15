/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.InputEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.*;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.utils.Material;

import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class AfkNick extends Feature {

	private long lastEvent = 0;
	private boolean manuallyAFK = false;
	private boolean isAFK = false;

	private final StringSetting nickName = new StringSetting()
		.name("Nick")
		.description("Wie du genickt werden willst, wenn du AFK bist." +
			"\n%name% wird mit deinem Namen ersetzt.")
		.defaultValue("AFK_%name%")
		.icon(Material.BOOK_AND_QUILL);

	private final KeySetting triggerAfk = new KeySetting()
		.name("Hotkey")
		.icon("key")
		.description("Markiert dich automatisch als AFK, wenn diese Taste gedrück wird.")
		.pressCallback(b -> {
			if (!b)
				return;

			isAFK = manuallyAFK = true;
			lastEvent = 0;
			send("/nick " + nickName.get().replace("%name%", name()));
		});

	private final NumberSetting minutes = new NumberSetting()
		.name("Minuten")
		.description("Nach wie vielen Minuten du als AFK eingestuft werden sollst.")
		.defaultValue(5)
		.icon(Material.WATCH);

	private final NumberSetting seconds = new NumberSetting()
		.name("Sekunden")
		.description("Nach wie vielen Sekunden du als AFK eingestuft werden sollst.")
		.icon(Material.WATCH);

	private final StringSetting messageReplay = new StringSetting()
		.name("Nachricht-\nbeantworter")
		.description("Mit welcher Nachricht geantwortet wird, wenn dir jemand eine /msg schreibt, während du AFK bist."
			+ "\n(Leerlassen zum deaktivieren)")
		.icon(Material.BOOK_AND_QUILL);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch Nicken wenn AFK")
		.description("Nickt dich, wenn du eine bestimmte, einstellbare Zeit AFK bist.")
		.icon("labymod:settings/modules/afk_timer")
		.subSettings(nickName, messageReplay, triggerAfk, new HeaderSetting(), minutes, seconds);

	@EventListener(triggerWhenDisabled = true)
	private void onKeyboardInput(InputEvent.KeyInputEvent event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onServerSwitch(ServerSwitchEvent event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener
	private void onMsg(MessageReceiveEvent event) {
		if (!isAFK || messageReplay.get().isEmpty())
			return;

		Matcher matcher = Constants.MESSAGE_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches())
			return;

		String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
		if (name.equals(name()))
			return;

		send("/msg " + name + " " + messageReplay.get());
	}

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (player().motionX > 0 || player().motionY > 0 || player().motionZ > 0) {
			lastEvent = System.currentTimeMillis();
			return;
		}

		if (lastEvent == 0 || (minutes.get() == 0 && seconds.get() == 0))
			return;

		// Check settings are currently being edited
		if (mc().currentScreen instanceof LabyModAddonsGui && !path().isEmpty() && path().get(path().size() - 1) == enabled)
			return;

		long diff = System.currentTimeMillis() - lastEvent;

		if (diff > (minutes.get() * 60 + seconds.get()) * 1000) {
			if (isAFK)
				return;

			isAFK = true;
			send("/nick " + nickName.get().replace("%name%", name()));
			return;
		}

		if (!isAFK)
			return;

		isAFK = manuallyAFK = false;
		send("/unnick");
	}

}
