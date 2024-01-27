/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_reactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.ChatEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.util.ChatLineUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class ChatReactor extends Feature {

	private static boolean loaded = false;

	private static final EntryAddSetting newEntrySetting = EntryAddSetting.create()
		.name("Neue Reaktion erstellen")
		.callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddChatReactionGui(null, Minecraft.getMinecraft().currentScreen)));

	@MainElement(configureSubSettings = false)
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("ChatReactor")
		.description("Führt bei Chatnachrichten Befehle aus.")
		.icon("cpu")
		.subSettings(HeaderSetting.create("Reaktionen"), newEntrySetting);

	public ChatReactor() {
		loadEntries();
	}

	private static List<BaseSetting> getPath() {
		return Collections.emptyList();//TODO: Reflection.get(mc().currentScreen, "path");
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		JsonArray array = new JsonArray();
		for (BaseSetting element : enabled.getSubSettings()) {
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
				new ReactionDisplaySetting(reaction, enabled);
			}
		}

		loaded = true;
	}

	@EventListener(priority = Priority.LOWEST)
	public void onMsg(ChatEvent.ChatMessageAddEvent event) {
		if ((mc().currentScreen instanceof Object /*TODO: LabyModAddonsGui*/ && getPath().contains(getMainElement()))
			|| mc().currentScreen instanceof AddChatReactionGui)
			return;

		IChatComponent component = ChatLineUtil.getUnmodifiedIChatComponent(event.component);

		for (BaseSetting element : enabled.getSubSettings()) {
			if (!(element instanceof ReactionDisplaySetting))
				continue;

			ReactionDisplaySetting setting = (ReactionDisplaySetting) element;
			ChatReaction reaction = setting.reaction;

			if (!true /*TODO: reaction.citybuild.isOnCb()*/)
				continue;

			try {
				reaction.processMessage(component.getFormattedText());
			} catch (Exception e) {
				//TODO: display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + reaction.command + "\" existiert nicht in \"" + reaction.trigger + "\"");
				setting.set(false);
			}
		}
	}
}