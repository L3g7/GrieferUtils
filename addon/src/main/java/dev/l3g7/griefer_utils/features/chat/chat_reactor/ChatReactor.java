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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ChatReactor extends Feature {

	private static boolean loaded = false;

	private static final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Neue Reaktion erstellen")
		.callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddChatReactionGui(null, Minecraft.getMinecraft().currentScreen)));

	@MainElement(configureSubSettings = false)
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("ChatReactor")
		.description("Führt bei Chatnachrichten Befehle aus.")
		.icon("cpu")
		.subSettings(new HeaderSetting("Reaktionen"), newEntrySetting);

	public ChatReactor() {
		loadEntries();
	}

	private static List<SettingsElement> getPath() {
		return Reflection.get(mc().currentScreen, "path");
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		JsonArray array = new JsonArray();
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (element instanceof ReactionDisplaySetting)
				array.add(((ReactionDisplaySetting) element).reaction.toJson());
		}

		Config.set("chat.chat_reactor.entries", array);
		Config.save();
	}

	private void loadEntries() {

		String path = "chat.chat_reactor.entries";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				ChatReaction reaction = ChatReaction.fromJson(jsonElement.getAsJsonObject());
				new ReactionDisplaySetting(reaction, enabled).icon(reaction.regEx ? "regex" : "yellow_t");
			}
		}

		loaded = true;
	}

	@EventListener(priority = EventPriority.LOWEST)
	public void onMsg(ClientChatReceivedEvent event) {
		if ((mc().currentScreen instanceof LabyModAddonsGui && getPath().contains(getMainElement()))
			|| mc().currentScreen instanceof AddChatReactionGui || event.type == 2)
			return;

		String srv = MinecraftUtil.getServerFromScoreboard();

		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ReactionDisplaySetting))
				continue;

			ReactionDisplaySetting setting = (ReactionDisplaySetting) element;
			ChatReaction reaction = setting.reaction;

			if (!reaction.cityBuild.equals("Jeder CB") && !srv.equals(reaction.cityBuild))
				continue;

			try {
				reaction.processMessage(event.message.getFormattedText());
			} catch (Exception e) {
				display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + reaction.command + "\" existiert nicht in \"" + reaction.trigger + "\"");
				setting.set(false);
			}
		}
	}
}