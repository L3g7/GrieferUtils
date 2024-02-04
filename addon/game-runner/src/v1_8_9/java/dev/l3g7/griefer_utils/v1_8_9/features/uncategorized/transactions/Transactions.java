/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.transactions;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModConnectionEvent.MMPacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModConnectionEvent.MMStateChangeEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.MysteryModConnection.State;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.Transaction;
import dev.l3g7.griefer_utils.v1_8_9.misc.mysterymod_connection.packets.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.minecraft.init.Items;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.uuid;

@Singleton
public class Transactions extends Feature { // NOTE: search, export

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final List<BaseSetting<?>> HEADER = Arrays.asList(
		HeaderSetting.create("§r"),
		HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		HeaderSetting.create("§f§lTransaktionen").scale(.7).entryHeight(7),
		HeaderSetting.create("§fder letzten 30 Tage").scale(.7).entryHeight(10),
		HeaderSetting.create("§c§nDie Beträge sind abgerundet§c!").scale(.7)
	);

	private final Set<Transaction> transactions = Collections.synchronizedSet(new TreeSet<>());

	private SettingWidget settingWidget;

	@MainElement
	private final CategorySetting setting = CategorySetting.create()
		.name("Transaktionen")
		.description("§eVerbindet...")
		.icon("scroll")
		.disable()
		.subSettings(HEADER);

	@EventListener
	public void onMMPacket(MMPacketReceiveEvent<TransactionsPacket> event) {
		transactions.addAll(event.packet.transactions);
		updateSettings();
	}

	@EventListener
	public void onMMStateChange(MMStateChangeEvent event) {
		State state = event.state;
		if (state == State.CONNECTED) {
			setting.name("Transaktionen")
				.description("Zeigt die Transaktionen der letzen 30 Tage an.")
				.enable();

			// Send Transaction packet every 3s
			MysteryModConnection.eventLoopGroup.scheduleAtFixedRate(() -> event.ctx.writeAndFlush(new RequestTransactionsPacket(uuid())), 0, 3, TimeUnit.SECONDS);
		} else {
			setting.name("§cTransaktionen")
				.description("§cMysteryMod ist nicht erreichbar:", state.errorMessage, "", "Joine auf einen Server, um die Verbindung erneut zu versuchen!")
				.disable();
		}

		if (settingWidget != null)
			Laby.labyAPI().minecraft().executeOnRenderThread(() -> settingWidget.reInitialize());
	}

	@EventListener
	public void onInit(SettingActivityInitEvent event) {
		// Grab settingWidget for reloading
		if (event.holder() == ((CategorySettingImpl) setting).parent()) {
			for (Widget child : event.settings().getChildren()) {
				if (child instanceof SettingWidget widget && widget.setting() == setting) {
					settingWidget = widget;
				}
			}
		}

		// Fix title for subsettings
		else if (event.holder().parent() == setting) {
			ComponentWidget titleWidget = event.get("setting-header", "title");
			TextComponent title = (TextComponent) titleWidget.component();
			String id = Reflection.<TextComponent>get(event.settings().getChildren().remove(0), "displayName").getText();

			title.text(title.getText().replaceAll("§[aec]", "§f") + "§r §7(#" + id + ")");

		}
	}

	private void updateSettings() {
		List<BaseSetting<?>> list = setting.getSubSettings();

		list.clear();
		list.add(HeaderSetting.createText("Transaktionen der letzten 30 Tage", "Die Beträge sind abgerundet!").center());

		// Add transactions count
		list.add(HeaderSetting.create("§r"));
		list.add(HeaderSetting.create("Insgesamt " + (transactions.size() == 1 ? "eine Transaktion" : transactions.size() + " Transaktionen")));

		// Add transactions
		List<Transaction> transactions = new ArrayList<>(this.transactions);
		for (Transaction t : transactions) {
			if (t.recipientname == null || t.recipientname.equals("muchelchen") || t.recipientname.equals("1Stocki") || t.username.equals("L3g73"))
				continue;

			Direction direction = Direction.get(t);

			String amountStr = Constants.DECIMAL_FORMAT_98.format(t.amount);
			String title = "§l" + amountStr + "$§";

			List<BaseSetting<?>> subSettings = new ArrayList<>();
			subSettings.add(HeaderSetting.create(String.valueOf(t.id))); // Removed and merged into header by onInit method

			String icon = "outgoing_gray";

			// Add sender/receiver by direction
			switch (direction) {
				case SENT:
					title = "§c" + title + "c an §l" + t.recipientname;
					icon = "outgoing";
					subSettings.add(CategorySetting.create().name("§lEmpfänger: §r" + t.recipientname).icon("yellow_name"));
					break;
				case RECEIVED:
					title = "§a" + title + "a von §l" + t.username;
					icon = "ingoing";
					subSettings.add(CategorySetting.create().name("§lSender: §r" + t.username).icon("yellow_name"));
					break;
				case SELF:
					title = "§e" + title + "e an dich";
					icon = "inoutgoing";
					break;
			}

			// Add amount and timestamp
			subSettings.add(CategorySetting.create().name("§lBetrag: §r" + amountStr + "$").icon("coin_pile"));
			subSettings.add(CategorySetting.create().name("§lZeitpunkt: §r" + DATE_FORMAT.format(new Date(t.timestamp))).icon(Items.clock));

			list.add(CategorySetting.create().name(" " + title).icon("wallets/" + icon).subSettings(subSettings));
		}

		// Update
		setting.subSettings(list.toArray(BaseSetting[]::new));
		if (settingWidget != null)
			Laby.labyAPI().minecraft().executeOnRenderThread(() -> settingWidget.reInitialize());
	}

	public enum Direction {

		SENT, RECEIVED, SELF;

		public static Direction get(Transaction t) {
			if (t.recipientname.equals(t.username))
				return SELF;
			if (MinecraftUtil.name().equals(t.username))
				return SENT;
			else
				return RECEIVED;
		}
	}

}