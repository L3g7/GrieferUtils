/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_reactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatMessage;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
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

	public static void triggerReactions(IChatComponent component) {
		if ((mc().currentScreen instanceof Object /*TODO: LabyModAddonsGui && getPath().contains(getMainElement() )*/)
			|| mc().currentScreen instanceof AddChatReactionGui)
			return;

		for (BaseSetting element : enabled.getSubSettings()) {
			if (!(element instanceof ReactionDisplaySetting setting))
				continue;

			ChatReaction reaction = setting.reaction;
			if (reaction.citybuild.isOnCb())
				continue;

			try {
				reaction.processMessage(component.getFormattedText());
			} catch (Exception e) {
				display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + reaction.command + "\" existiert nicht in \"" + reaction.trigger + "\"");
				setting.set(false);
			}
		}
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			if (!refresh)
				triggerReactions(component);
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = GuiNewChat.class, remap = false)
	private static class MixinGuiNewChat {

		@Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
		private void injectPrintChatMessageWithOptionalDeletion(IChatComponent lvt_1_1_, int lvt_2_1_, CallbackInfo ci) {
			ChatMessage chatMessage = Laby.labyAPI().chatProvider().chatController().messageAt(0);
			triggerReactions((IChatComponent) chatMessage);
		}

	}

}