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

package dev.l3g7.griefer_utils.features.chat.encrypted_messages;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.ChatLogModifyEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderChatEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.SplitLongMessages;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.UUID;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.misc.Constants.MESSAGE_RECEIVE_PATTERN;
import static dev.l3g7.griefer_utils.core.misc.Constants.MESSAGE_SEND_PATTERN;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;
import static net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND;

@Singleton
public class EncryptedMessages extends Feature {

	public static final String HANDSHAKE_START_SUFFIX = "[)+`3dD}";
	public static final String MESSAGE_SUFFIX = "4^7)'g,a";
	static final String LOCK = "§r[§g§u§e§m§f§r  ]§r ";
	static final String BROKEN_LOCK = "§r[§g§u§e§m§b§r  ]§r ";
	static String message = null;
	private static String previousCounterpart;

	private final BooleanSetting logMessages = new BooleanSetting()
		.name("Nachrichten in den Log schreiben")
		.description("Ob die Nachrichten (unverschlüselt) im Log gespeichert werden sollen")
		.icon(Material.BOOK_AND_QUILL);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Verschlüsselte Chatnachrichten")
		.description("Ermöglicht das Verschicken von verschlüsselten Direkt-Nachrichten. Syntax:" +
			"\n/emsg <Name> <Nachricht>" +
			"\n/er <Nachricht>")
		.icon("lock")
		.defaultValue(true)
		.subSettings(logMessages);

	@EventListener
	public void onMessageSend(MessageEvent.MessageSendEvent event) {
		event.setCanceled(processMessage(event));
	}

	public boolean processMessage(MessageEvent.MessageSendEvent event) {
		String msg = event.message.trim().toLowerCase();

		if (msg.startsWith("/er")) {
			if (msg.equals("/er"))
				return fail("Syntax: /er <Nachricht>");

			String[] parts = msg.split(" ");
			if (parts.length < 2)
				return fail("Syntax: /er <Nachricht>");

			UUID uuid = PlayerUtil.isValid(previousCounterpart) ? PlayerUtil.getUUID(previousCounterpart) : null;
			if (uuid == null)
				return fail(previousCounterpart + " ist nicht online!");

			send("/msg %s %s%s", previousCounterpart, EncryptedSessions.encrypt(uuid, event.message.trim().substring(4)), MESSAGE_SUFFIX);

			return true;
		}

		if (!msg.startsWith("/emsg"))
			return false;

		if (msg.equals("/emsg"))
			return fail("Syntax: /emsg <Name> <Nachricht>");

		String[] parts = msg.split(" ");
		if (parts.length < 3)
			return fail("Syntax: /emsg <Name> <Nachricht>");

		if (parts[1].startsWith("!"))
			return fail("Bedrock-Spielern können kein GrieferUtils haben!");

		if (parts[1].equalsIgnoreCase(player().getName()))
			return fail("Du kannst keine Selbstgespräche führen!");

		UUID uuid = PlayerUtil.isValid(parts[1]) ? PlayerUtil.getUUID(parts[1]) : null;
		if (uuid == null)
			return fail("Dieser Spieler ist nicht online!");

		if (!EncryptedSessions.sessionExists(uuid)) {
			EncryptedMessages.message = event.message;
			HandShaker.startHandShake(uuid);
			return true;
		}

		int messageStart = parts[0].length() + parts[1].length() + 2;
		send("/msg %s %s%s", parts[1], EncryptedSessions.encrypt(uuid, event.message.trim().substring(messageStart)), MESSAGE_SUFFIX);

		return true;
	}

	/**
	 * Decrypts messages
	 */
	@EventListener(priority = EventPriority.HIGHEST)
	public void onMessageModfiy(MessageEvent.MessageModifyEvent event) {
		if (!event.original.getUnformattedText().endsWith(MESSAGE_SUFFIX))
			return;

		Matcher matcher = MESSAGE_RECEIVE_PATTERN.matcher(event.original.getFormattedText());
		boolean receiving = true;
		if (!matcher.matches()) {
			matcher = MESSAGE_SEND_PATTERN.matcher(event.original.getFormattedText());
			receiving = false;
			if (!matcher.matches())
				return;
		}

		String name = matcher.group("name").replaceAll("§.", "");
		UUID uuid = name.contains("~") ? NameCache.getUUID(name) : PlayerUtil.getUUID(name);
		if (uuid == null) {
			System.err.println("[GrieferUtils] Message decryption failed: No uuid was found");
			addLock(event, " (UUID konnte nicht gefunden werden)");
			return;
		}

		if (!EncryptedSessions.sessionExists(uuid)) {
			System.err.println("[GrieferUtils] Message decryption failed: No session was found");
			addLock(event, " (Es wurde keine verschlüsselte Verbindung aufgebaut)");
			return;
		}

		previousCounterpart = name;

		String message = matcher.group("message").replaceAll("§.", "");
		try {
			String decryptedMessage = EncryptedSessions.decrypt(uuid, message.substring(0, message.length() - MESSAGE_SUFFIX.length()));

			// Make sure the encryption ciphers are in the same state
			if (receiving)
				EncryptedSessions.encrypt(uuid, decryptedMessage);

			for (IChatComponent component : event.message.getSiblings()) {
				if (component instanceof ChatComponentText) {
					if (component.getUnformattedTextForChat().equals(message)) {
						Reflection.set(component, decryptedMessage, "text");
						break;
					}
				}
			}
			addLock(event, null);
		} catch (Exception e) {
			e.printStackTrace();
			addLock(event, "");
		}
	}

	/**
	 * Handles handshake messages
	 */
	@EventListener
	public void onMessageReceive(ClientChatReceivedEvent event) {
		String text = event.message.getUnformattedText();
		if (!text.endsWith(HANDSHAKE_START_SUFFIX))
			return;

		Matcher matcher = MESSAGE_RECEIVE_PATTERN.matcher(event.message.getFormattedText());

		if (!matcher.matches()) {
			if (MESSAGE_SEND_PATTERN.matcher(event.message.getFormattedText()).matches())
				event.setCanceled(true);

			return;
		}

		event.setCanceled(true);

		String name = matcher.group("name").replaceAll("§.", "");
		String message = matcher.group("message").replaceAll("§.", "");
		String data = message.substring(0, message.length() - HANDSHAKE_START_SUFFIX.length());

		new Thread(() -> HandShaker.onHandShakeReceive(name, data)).start();
	}

	@EventListener
	public void renderLock(RenderChatEvent event) {
		IChatComponent component = (IChatComponent) event.chatLine.getComponent();
		String formattedText = component.getFormattedText();

		int idStart = formattedText.indexOf("§g§u§e§m");
		if (idStart == -1)
			return;

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		boolean broken = formattedText.charAt(idStart + 9) == 'b';
		drawUtils.bindTexture(new ResourceLocation("griefer_utils/icons/" + (broken ? "broken_" : "") + "lock.png"));

		int x = drawUtils.getStringWidth(formattedText.substring(0, idStart)) + 1;
		drawUtils.drawTexture(x - 0.5, event.y - 8, 0, 0, 256, 256, 8, 8, event.alpha);
	}

	/**
	 * Handles the reduced maximum message length
	 */
	@EventListener
	public void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!(event.gui instanceof GuiChat))
			return;

		GuiTextField inputField = Reflection.get(event.gui, "inputField");
		String text = inputField.getText();

		if (text.startsWith("/er ")) {
			inputField.setMaxStringLength(52);
			return;
		}

		if (!text.startsWith("/emsg ")) {
			if (!FileProvider.getSingleton(SplitLongMessages.class).isEnabled())
				inputField.setMaxStringLength(100);
			return;
		}

		String[] parts = text.split(" ");
		if (parts.length < 2)
			return;

		inputField.setMaxStringLength(parts[1].length() + 55);
	}

	/**
	 * Handles logging
	 */
	@EventListener
	public void onLog(ChatLogModifyEvent event) {
		if (logMessages.get())
			return;

		event.setCanceled(event.message.contains("§r[§g§u§e§m§"));
	}

	private boolean fail(String message) {
		player().addChatMessage(new ChatComponentText(Constants.ADDON_PREFIX + BROKEN_LOCK + "§c" + message));
		return true;
	}

	private void addLock(MessageEvent.MessageModifyEvent event, String failMessage) {
		String message = failMessage == null ? "§aDie Nachricht konnte erfolgreich entschlüsselt werden." : "§cDie Nachricht konnte nicht entschlüsselt werden!" + failMessage;

		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(message));
		IChatComponent component = new ChatComponentText(failMessage == null ? LOCK : BROKEN_LOCK);
		component.getChatStyle().setChatHoverEvent(hoverEvent);

		event.message = component.appendSibling(event.message);
		for (IChatComponent iChatComponent : event.message) {
			ClickEvent clickEvent = iChatComponent.getChatStyle().getChatClickEvent();
			if (clickEvent != null
				&& clickEvent.getAction() == SUGGEST_COMMAND
				&& clickEvent.getValue().startsWith("/msg "))
				iChatComponent.getChatStyle().setChatClickEvent(new ClickEvent(SUGGEST_COMMAND, clickEvent.getValue().replace("/msg", "/emsg")));
		}
	}

}
