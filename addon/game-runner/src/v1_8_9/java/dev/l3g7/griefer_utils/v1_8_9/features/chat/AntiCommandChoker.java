/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.StringListSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageSendEvent;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class AntiCommandChoker extends Feature {

	private static final String COMMAND = "/grieferutils_anti_command_choker ";
	private static final Pattern FAIL_PATTERN = Pattern.compile("^[(7](?=[a-zA-Z])|^[\\w(][/(]|^&(?=[^a-f0-9])");

	private final StringListSetting customEntries = StringListSetting.create()
		.name("Eigene Einträge")
		.description("Wenn eine Nachricht mit einem dieser Einträge beginnt, wird sie abgefangen.")
		.icon(Items.paper)
		.entryIcon(Items.paper)
		.placeholder("Abzufangende Nachricht");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("AntiCommandChoker")
		.description("""
			Verhindert das Senden von falsch geschriebenen Befehlen.
			Befehle, die standardmäßig abgefangen werden (Beispiele):
			7p h
			(p h
			&p h
			t/p h
			t(p h""")
		.icon(new ItemStack(Blocks.barrier, 7))
		.subSettings(LABY_4.isActive() ? null : HeaderSetting.create("§e§lEigene Einträge").scale(0.7), customEntries);

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		String msg = event.message;
		if (msg.startsWith(COMMAND)) {
			event.cancel();
			String message = msg.substring(COMMAND.length());
			int id = Integer.parseInt(message.split(" ")[0]);

			labyBridge.removeChatLine(id);

			// Repeat message
			int idLength = Integer.toString(id).length();
			if (message.length() == idLength)
				return; // Command canceled

			String command = message.substring(idLength + 1);

			mc().getNetHandler().addToSendQueue(new C01PacketChatMessage(command));

			labyBridge.replaceLastSentMessage(command);
			return;
		}

		for (String s : customEntries.get()) {
			if (!msg.startsWith(s))
				continue;

			int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

			IChatComponent question = new ChatComponentText(Constants.ADDON_PREFIX + String.format("Deine Nachricht beginnt mit %s. Soll sie trotzdem abgeschickt werden? ", s));

			IChatComponent yes = new ChatComponentText("§a[§l✔§r§a] ").setChatStyle(new ChatStyle()
				.setChatClickEvent(getClickEvent(msg, id)));

			IChatComponent no = new ChatComponentText("§c[✖]").setChatStyle(new ChatStyle()
				.setChatClickEvent(getClickEvent(null, id)));

			mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(question.appendSibling(yes).appendSibling(no), id);
			event.cancel();
			return;
		}

		Matcher matcher = FAIL_PATTERN.matcher(msg);
		if (!matcher.find())
			return;

		int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
		String fixedCommand = msg.substring(matcher.end());

		IChatComponent question = new ChatComponentText(Constants.ADDON_PREFIX + String.format("Meintest du /%s? ", fixedCommand.split(" ")[0]));

		IChatComponent yes = new ChatComponentText("§a[§l✔§r§a] ").setChatStyle(new ChatStyle()
			.setChatClickEvent(getClickEvent("/" + fixedCommand, id)));

		IChatComponent no = new ChatComponentText("§c[✖]").setChatStyle(new ChatStyle()
			.setChatClickEvent(getClickEvent(msg, id)));

		mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(question.appendSibling(yes).appendSibling(no), id);
		event.cancel();
	}

	private ClickEvent getClickEvent(String command, int id) {
		String cmd = COMMAND + id;
		if (command != null)
			cmd += " " + command;

		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
	}

}
