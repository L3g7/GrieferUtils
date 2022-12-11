/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.utils.Material;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST;

@Singleton
public class ChatTime extends Feature {

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();
	private boolean styleValid = false;
	private boolean formatValid = false;

	private final StringSetting style = new StringSetting()
		.name("Design")
		.icon(Material.EMPTY_MAP)
		.callback(v -> styleValid = v.contains("%s"));

	private final StringSetting format = new StringSetting()
		.name("Zeitformat")
		.icon(Material.EMPTY_MAP)
		.callback(v -> {
			try {
				DATE_FORMAT.applyPattern(v);
				formatValid = true;
			} catch (IllegalArgumentException e) {
				formatValid = false;
			}
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("ChatTime")
		.icon(Material.WATCH)
		.subSettings(style, format);

	public ChatTime() {
		if(enabled.getStorage().value == null) { // If no value loaded, try loading from TebosBrime's addon
			File configFile = new File(mc().mcDataDir, "LabyMod/addons-1.8/config/ChatTime.json");
			if(configFile.exists()) {
				IOUtil.read(configFile).asJsonObject().ifPresent(obj -> {
					JsonObject cfg = obj.get("config").getAsJsonObject();
					format.defaultValue(cfg.has("chatData") ? cfg.get("chatData").getAsString() : "HH:mm:ss");
					style.defaultValue(cfg.has("chatData2") ? cfg.get("chatData2").getAsString().replace("%time%", "%s") : "&4[&e%s&4a]");
				});
			}
		}
		format.defaultValue("HH:mm:ss");
		style.defaultValue("&7[&6%s&7] ");
	}

	@EventListener(priority = LOWEST)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!styleValid || !formatValid)
			return;

		event.message = new ChatComponentText(String.format(style.get(), DATE_FORMAT.format(new Date())).replace('&', '§') + "§r").appendSibling(event.message);
	}

}
