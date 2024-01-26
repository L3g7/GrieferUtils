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

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.NameCache;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class SlowChatCooldown extends Feature {

	private long timeoutEnd = -1;
	private boolean isSlowChatEnabled = false;
	private IChatComponent originalMessage = null;
	private long originalDisplayEnd = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("SlowChat-Cooldown")
		.description("Zeigt dir den 10s-Cooldown nach dem Schreiben einer Nachricht bei aktiviertem SlowChat in der Actionbar an.")
		.icon("hourglass");

	@EventListener(triggerWhenDisabled = true)
	public void onMessage(MessageReceiveEvent event) {
		if (event.type == 2 && timeoutEnd > 0) {
			originalDisplayEnd = System.currentTimeMillis() + 3_000;
			originalMessage = event.message;
			event.cancel();
			return;
		}

		String msg = event.message.getFormattedText();

		if (msg.equals("§r§8[§r§6GrieferGames§r§8] §r§eDu kannst nur jede 10 Sekunden schreiben.§r"))
			isSlowChatEnabled = true;
		else if (msg.startsWith("§r§8[§r§6Chat§r§8] §r§e§lDer Chat wurde von §r"))
			isSlowChatEnabled = msg.endsWith("§r§e§l verlangsamt.§r");
		else if (msg.equals("§r§cPlease do not repeat the same (or similar) message.§r"))
			timeoutEnd = System.currentTimeMillis() + 10_000;
		else {
			for (Pattern p : new Pattern[] {Constants.GLOBAL_RECEIVE_PATTERN, Constants.PLOTCHAT_RECEIVE_PATTERN}) {
				Matcher matcher = p.matcher(msg);
				if (!matcher.matches())
					continue;

				String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
				if (MinecraftUtil.name().equals(name))
					timeoutEnd = System.currentTimeMillis() + 10_000;
			}
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		timeoutEnd = -1;
		isSlowChatEnabled = false;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerQuit(ServerEvent.ServerQuitEvent event) {
		timeoutEnd = -1;
		isSlowChatEnabled = false;
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null || !isSlowChatEnabled)
			return;

		if (timeoutEnd < 0)
			return;

		long diff = timeoutEnd - System.currentTimeMillis();
		if (diff < 0) {
			endCooldown();
			return;
		}

		float remainingTime = (int) Math.ceil(diff / 100f) / 10f;
		mc().ingameGUI.setRecordPlaying("§6SlowChat-Cooldown: §e " + remainingTime + "s", false);
	}

	private void endCooldown() {
		timeoutEnd = -1;
		mc().ingameGUI.setRecordPlaying("", false);

		if (originalDisplayEnd > System.currentTimeMillis()) {
			mc().ingameGUI.setRecordPlaying(originalMessage, false);
			int time = (int) ((originalDisplayEnd - System.currentTimeMillis()) / 50);
			Reflection.set(mc().ingameGUI, "recordPlayingUpFor", time);
		}
	}

}
