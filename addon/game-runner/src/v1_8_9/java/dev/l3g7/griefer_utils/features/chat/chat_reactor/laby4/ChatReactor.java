/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_reactor.laby4;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.util.ChatLineUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_4)
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

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		JsonArray array = new JsonArray();
		for (BaseSetting<?> element : enabled.getChildSettings()) {
			if (element instanceof ReactionDisplaySetting)
				array.add(((ReactionDisplaySetting) element).reaction.toJson());
		}

		Config.set("chat.chat_reactor.entries", array);
		Config.save();
	}

	private void loadEntries() {
		String path = "chat.chat_reactor.entries";
		if (Config.has(path))
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray())
				enabled.addSetting(new ReactionDisplaySetting(ChatReaction.fromJson(jsonElement.getAsJsonObject())));

		loaded = true;
	}

	public static void triggerReactions(IChatComponent component) {
		if (Laby4Util.isSettingOpened(enabled)
			|| mc().currentScreen instanceof AddChatReactionGui)
			return;

		component = ChatLineUtil.getUnmodifiedIChatComponent(component);
		if (component == null)
			return;

		for (BaseSetting<?> element : enabled.getChildSettings()) {
			if (!(element instanceof ReactionDisplaySetting setting))
				continue;

			ChatReaction reaction = setting.reaction;
			if (!reaction.citybuild.isOnCb())
				continue;

			try {
				reaction.processMessage(component.getFormattedText());
			} catch (Exception e) {
				display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + reaction.command + "\" existiert nicht in \"" + reaction.trigger + "\"");
				setting.set(false);
			}
		}
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = GuiNewChat.class, remap = false)
	private static class MixinGuiNewChat {

		@Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
		private void injectPrintChatMessageWithOptionalDeletion(IChatComponent lvt_1_1_, int lvt_2_1_, CallbackInfo ci) {
			ChatMessage chatMessage = Laby.labyAPI().chatProvider().chatController().messageAt(0);
			triggerReactions((IChatComponent) chatMessage.component());
		}

	}

}