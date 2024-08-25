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

package dev.l3g7.griefer_utils.features.chat.chat_reactor.laby3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.temp.TempEntryAddSetting;
import dev.l3g7.griefer_utils.core.util.ChatLineUtil;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_3)
public class ChatReactor extends Feature {

	private static boolean loaded = false;

	private static final TempEntryAddSetting newEntrySetting = new TempEntryAddSetting()
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

	private static List<SettingsElement> getPath() {
		return Reflection.get(mc().currentScreen, "path");
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		JsonArray array = new JsonArray();
		for (SettingsElement element : ((SettingsElement) enabled).getSubSettings().getElements()) {
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
				new ReactionDisplaySetting(reaction, (SettingsElement) enabled);
			}
		}

		loaded = true;
	}

	public static void onMsg(IChatComponent eventComponent) {
		ChatReactor self = FileProvider.getSingleton(ChatReactor.class);
		if (!(self.isEnabled()))
			return;

		if ((mc().currentScreen instanceof LabyModAddonsGui && getPath().contains((SettingsElement) self.getMainElement()))
			|| mc().currentScreen instanceof AddChatReactionGui)
			return;

		IChatComponent component = ChatLineUtil.getUnmodifiedIChatComponent(eventComponent);
		if (component == null)
			return;

		for (SettingsElement element : ((SettingsElement) enabled).getSubSettings().getElements()) {
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

	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			if (!refresh)
				onMsg(component);
		}

	}

}