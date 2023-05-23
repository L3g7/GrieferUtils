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

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.utils.Material;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;

@Singleton
public class AfkNick extends Feature {

	private long lastEvent = 0;
	private boolean isAFK = false;

	private final StringSetting nickName = new StringSetting()
		.name("Nick")
		.description("Wie du genickt werden willst, wenn du AFK bist." +
			"\n%name% wird mit deinem Namen ersetzt.")
		.defaultValue("AFK_%name%")
		.icon(Material.BOOK_AND_QUILL);

	private final NumberSetting minutes = new NumberSetting()
		.name("Minuten")
		.description("Nach wie vielen Minuten du als AFK eingestuft werden sollst.")
		.defaultValue(5)
		.icon(Material.WATCH);

	private final NumberSetting seconds = new NumberSetting()
		.name("Sekunden")
		.description("Nach wie vielen Sekunden du als AFK eingestuft werden sollst.")
		.icon(Material.WATCH);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("AFK Nick")
		.description("Nickt dich, wenn du eine bestimmte, einstellbare Zeit AFK bist.")
		.icon("labymod:settings/modules/afk_timer")
		.subSettings(nickName, new HeaderSetting(), minutes, seconds);

	@EventListener(triggerWhenDisabled = true)
	public void onInput(InputEvent event) {
		lastEvent = System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent event) {
		lastEvent = System.currentTimeMillis();
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (player().motionX > 0 || player().motionY > 0 || player().motionZ > 0) {
			lastEvent = System.currentTimeMillis();
			return;
		}

		long diff = System.currentTimeMillis() - lastEvent;

		if (diff > (minutes.get() * 60 + seconds.get()) * 1000) {
			if (isAFK)
				return;

			isAFK = true;
			send("/nick " + nickName.get().replace("%name%", player().getName()));
			return;
		}

		if (!isAFK)
			return;

		isAFK = false;
		send("/unnick");
	}

}
