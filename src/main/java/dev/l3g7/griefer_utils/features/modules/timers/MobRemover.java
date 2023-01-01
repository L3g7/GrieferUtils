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

package dev.l3g7.griefer_utils.features.modules.timers;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Util;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MobRemover extends Module {

	private static final Pattern MOB_REMOVER_PATTERN = Pattern.compile("§r§8\\[§r§6MobRemover§r§8] §r§4Achtung! §r§7In §r§e(?<minutes>\\d) Minuten §r§7werden alle Tiere gelöscht\\.§r");

	private final BooleanSetting shorten = new BooleanSetting()
		.name("Zeit kürzen")
		.icon(Material.LEVER)
		.config("modules.mob_remover.shorten");

	private final BooleanSetting warn = new BooleanSetting()
		.name("Warnen")
		.icon(Material.LEVER)
		.config("modules.mob_remover.warn");

	private long mobRemoverEnd = -1;

	public MobRemover() {
		super("MobRemover", "Zeigt dir die Zeit bis zum nächsten MobRemover an", "mob_remover", new IconData("griefer_utils/icons/skull_crossed_out.png"));
	}

	@Override
	public String[] getValues() {
		if (mobRemoverEnd == -1)
			return getDefaultValues();

		long diff = mobRemoverEnd - System.currentTimeMillis();
		if (diff < 0)
			return getDefaultValues();

		// Warn if mob remover is less than 20s away
		if (warn.get() && diff < 20 * 1000) {
			String s = Util.formatTime(mobRemoverEnd, true);
			if (!s.equals("0s"))
				title("§c§l" + s);
		}

		return new String[]{Util.formatTime(mobRemoverEnd, shorten.get())};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Unbekannt"};
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent p) {
		mobRemoverEnd = -1;
	}

	@EventListener
	public void onMessageReceive(ClientChatReceivedEvent event) {
		Matcher matcher = MOB_REMOVER_PATTERN.matcher(event.message.getFormattedText());
		if(matcher.matches())
			mobRemoverEnd = System.currentTimeMillis() + Long.parseLong(matcher.group("minutes")) * 60L * 1000L;
		else if (event.message.getFormattedText().matches("^§r§8\\[§r§6MobRemover§r§8] §r§7Es wurden (?:§r§\\d+§r§7|keine) Tiere entfernt\\.§r$"))
			mobRemoverEnd = System.currentTimeMillis() + 15L * 60L * 1000L;
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(shorten);
		list.add(warn);
	}

	private void title(String title) {
		mc.ingameGUI.displayTitle("§cMobRemover!", null, -1, -1, -1);
		mc.ingameGUI.displayTitle(null, title, -1, -1, -1);
		mc.ingameGUI.displayTitle(null, null, 0, 2, 3);
	}

}