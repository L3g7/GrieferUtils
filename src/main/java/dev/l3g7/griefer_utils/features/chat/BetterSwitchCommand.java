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

package dev.l3g7.griefer_utils.features.chat;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.labymod.utils.Material;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class BetterSwitchCommand extends Feature {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("^/(?:cb|switch) ?(?:cb)?(\\w+)(?: (.*))?$", Pattern.CASE_INSENSITIVE);

	private static final List<ServerAlias> SERVER_ALIASES = ImmutableList.of(
		new ServerAlias("nature", "Nature", "n"),
		new ServerAlias("extreme", "Extreme", "x"),
		new ServerAlias("cbevil", "Evil", "e"),
		new ServerAlias("farm1", "Wasser", "w"),
		new ServerAlias("nether1", "Lava", "l"),
		new ServerAlias("eventserver", "Event", "v"));

	private String command = null;
	private boolean awaitingSendCommand = false;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Besseres /switch")
		.description("Verbessert den '/switch <cb>' Befehl durch Aliasse und einem optionalem Join-Text. (Siehe '/cb')", "", "Der Join-Text wird nach dem Beitreten automatisch in den Chat geschrieben")
		.icon(Material.COMPASS);

	@EventListener
	public void onMessageSend(MessageEvent.MessageSendEvent event) {
		String msg = event.message;

		if (awaitingSendCommand) {
			awaitingSendCommand = false;
			return;
		}

		Matcher matcher = COMMAND_PATTERN.matcher(msg);

		if (matcher.matches()) {
			String cb = matcher.group(1);
			event.setCanceled(true);

			if (cb.matches("^\\d+$")) {
				command = matcher.group(2);
				awaitingSendCommand = true;
				send("/switch cb" + cb);
				return;
			}

			for (ServerAlias alias : SERVER_ALIASES) {
				if (alias.matches(cb)) {
					command = matcher.group(2);
					awaitingSendCommand = true;
					send("/switch " + alias.targetCityBuild);
					return;
				}
			}
		}

		if (!msg.startsWith("/cb") && !msg.startsWith("/switch"))
			return;

		display(Constants.ADDON_PREFIX + "Syntax: '/switch <CB> [text]', '/cb <CB> [text]' oder '/cb<CB> [text]'.");
		display(Constants.ADDON_PREFIX + "§f§nAliases:");

		display(Constants.ADDON_PREFIX + "§fExtreme: 'x'");
		display(Constants.ADDON_PREFIX + "§7Nature: 'n'");
		display(Constants.ADDON_PREFIX + "§7Extreme: 'x'");
		display(Constants.ADDON_PREFIX + "§7Evil: 'e'");
		display(Constants.ADDON_PREFIX + "§7Wasser: 'w'");
		display(Constants.ADDON_PREFIX + "§7Lava: 'l'");
		display(Constants.ADDON_PREFIX + "§7Event: 'v'");

		event.setCanceled(true);
	}

	@EventListener
	public void onCityBuild(CityBuildJoinEvent event) {
		if (command == null)
			return;

		send(command);
		command = null;
	}

	private static class ServerAlias {

		private final String targetCityBuild;
		private final String displayName;
		private final String[] aliases;

		public ServerAlias(String targetCityBuild, String displayName, String... aliases) {
			this.targetCityBuild = targetCityBuild;
			this.displayName = displayName;
			this.aliases = aliases;
		}

		public boolean matches(Object obj) {
			if (!(obj instanceof String))
				return false;

			for (String alias : aliases)
				if (alias.equalsIgnoreCase((String) obj))
					return true;

			return displayName.equalsIgnoreCase((String) obj) || targetCityBuild.equalsIgnoreCase((String) obj);
		}

	}

}
