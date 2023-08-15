/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.modules.timers;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.List;

@Singleton
public class Chatlog extends Module {

	private boolean sentCmd = false;

	private final DropDownSetting<TimeFormat> timeFormat = new DropDownSetting<>(TimeFormat.class)
		.name("Zeitformat")
		.icon("hourglass")
		.config("modules.chatlog.time_format")
		.defaultValue(TimeFormat.LONG);

	private final BooleanSetting hide = new BooleanSetting()
		.name("Verstecken, wenn fertig")
		.description("Ob das Modul versteckt werden soll, wenn derzeit kein Cooldown existiert.")
		.icon("blindness")
		.config("modules.chatlog.hide");

	private long chatlogEnd = -1;

	public Chatlog() {
		super("Chatlog", "Zeigt dir den verbleibenden Cooldown bis zum nächsten /chatlog an.", "chatlog", new ControlElement.IconData(Material.WATCH));
	}

	@Override
	public String[] getValues() {
		return new String[]{Util.formatTime(chatlogEnd, timeFormat.get() == TimeFormat.SHORT)};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{timeFormat.get() == TimeFormat.SHORT ? "0s" : "0 Sekunden"};
	}

	@Override
	public boolean isShown() {
		return super.isShown() && (!hide.get() || chatlogEnd >= System.currentTimeMillis());
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageSend(MessageSendEvent event) {
		String msg = event.message.toLowerCase();

		sentCmd = msg.startsWith("/chatlog") && !event.message.trim().equals("/chatlog");
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!sentCmd)
			return;

		String msg = event.message.getUnformattedText();

		if (msg.startsWith("[Chat-Log] Der Chat-Log wurde erfolgreich gespeichert: ") || msg.equals("------------ Chat-Log-Hilfe ------------")) {
			chatlogEnd = System.currentTimeMillis() + 30_000;
			sentCmd = false;
		}
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(timeFormat);
		list.add(hide);
	}

	private enum TimeFormat {
		SHORT("Kurz"),
		LONG("Lang");

		private final String name;
		TimeFormat(String name) {
			this.name = name;
		}
	}
}
