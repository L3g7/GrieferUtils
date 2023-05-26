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

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class AntiCommandChoker extends Feature {

	private static final String COMMAND = "/grieferutils_anti_command_choker ";
	private static final IngameChatManager ICM = IngameChatManager.INSTANCE;

	private static final Map<String, Integer> FAIL_TO_START = ImmutableMap.of(
		"^7[^ ].*", 1,
		"^\\w/[^ ].*", 2,
		"^\\([^)]+$", 1
	);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("AntiCommandChoker")
		.description("Verhindert das Senden von falsch geschriebenen Befehlen.")
		.icon(new ItemStack(Blocks.barrier, 7));

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		String msg = event.message;
		if (msg.startsWith(COMMAND)) {

			String message = msg.substring(COMMAND.length());
			int id = Integer.parseInt(message.split(" ")[0]);
			String command = message.substring(Integer.toString(id).length() + 1);

			mc().getNetHandler().addToSendQueue(new C01PacketChatMessage(command));

			// Edit the sent message
			ICM.getSentMessages().remove(ICM.getSentMessages().size() - 1);
			ICM.addToSentMessages(command);

			// Remove the message (everywhere, since I don't know in which chatroom it is)
			for (ChatRenderer chatRenderer : ICM.getChatRenderers())
				clear(chatRenderer, id);
			clear(ICM.getMain(), id);
			clear(ICM.getSecond(), id);

			event.setCanceled(true);
			return;
		}

		for (Map.Entry<String, Integer> fail : FAIL_TO_START.entrySet()) {
			if (!msg.matches(fail.getKey()))
				continue;

			int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
			String fixedCommand = msg.substring(fail.getValue());

			IChatComponent question = new ChatComponentText(Constants.ADDON_PREFIX + String.format("Meintest du /%s? ", fixedCommand.split(" ")[0]));

			IChatComponent yes = new ChatComponentText("§a[§l\u2714§r§a] ").setChatStyle(new ChatStyle()
				.setChatClickEvent(getClickEvent("/" + fixedCommand, id)));

			IChatComponent no = new ChatComponentText("§c[\u2716]").setChatStyle(new ChatStyle()
				.setChatClickEvent(getClickEvent(msg, id)));

			mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(question.appendSibling(yes).appendSibling(no), id);
			event.setCanceled(true);
			return;
		}
	}

	private ClickEvent getClickEvent(String command, int id) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + id + " " + command);
	}

	private void clear(ChatRenderer renderer, int id) {
		renderer.getChatLines().removeIf(line -> line.getChatLineId() == id);
		renderer.getBackendComponents().removeIf(line -> line.getChatLineId() == id);
	}

}
