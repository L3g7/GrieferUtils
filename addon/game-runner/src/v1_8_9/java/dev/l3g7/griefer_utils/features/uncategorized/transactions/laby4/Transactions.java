/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.transactions.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.network.MysteryModConnectionEvent.MMPacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.MysteryModConnectionEvent.MMStateChangeEvent;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.MysteryModConnection.State;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.packets.transactions.Transaction;
import dev.l3g7.griefer_utils.core.misc.mysterymod_connection.packets.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.transactions.LocalTransactions;
import dev.l3g7.griefer_utils.features.uncategorized.transactions.TempTransactionsBridge;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.navigation.elements.ScreenNavigationElement;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.SettingsActivity;
import net.minecraft.init.Items;

import java.text.SimpleDateFormat;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Transactions extends Feature implements TempTransactionsBridge { // NOTE: search, export

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private Set<Transaction> transactions = Collections.synchronizedSet(new TreeSet<>());
	private boolean firstRequest = false;

	@MainElement
	private final CategorySetting setting = CategorySetting.create()
		.name("Transaktionen")
		.description("§eVerbindet...")
		.icon("scroll")
		.disable();

	@EventListener
	public void onMMPacket(MMPacketReceiveEvent<TransactionsPacket> event) {
		if (firstRequest && event.packet.transactions.isEmpty()) {
			transactions = LocalTransactions.transactions;
			firstRequest = false;
		} else {
			transactions.addAll(event.packet.transactions);
		}

		updateSettings();
	}

	@EventListener
	public void onMMStateChange(MMStateChangeEvent event) {
		State state = event.state;
		if (state == State.CONNECTED) {
			setting.name("Transaktionen")
				.description("Zeigt die Transaktionen der letzen 30 Tage an.")
				.enable();
			firstRequest = true;
			MysteryModConnection.requestTransactions();
		} else {
			setting.name("§cTransaktionen")
				.description("§cMysteryMod ist nicht erreichbar:", state.errorMessage, "", "Joine auf einen Server, um die Verbindung erneut zu versuchen!")
				.disable();
		}
	}

	@EventListener
	public void onInit(SettingActivityInitEvent event) {
		if (event.holder() == setting && !event.isReload) {
			if (transactions != LocalTransactions.transactions)
				MysteryModConnection.requestTransactions();
			else
				updateSettings();
		}

		// Fix title for subsettings
		else if (event.holder().parent() == setting) {
			ComponentWidget titleWidget = event.get("setting-header", "title");
			TextComponent title = (TextComponent) titleWidget.component();
			String id = Reflection.<TextComponent>get(event.settings().getChildren().remove(0), "displayName").getText();

			title.text(title.getText().replaceAll("§[aec]", "§f") + "§r §7(#" + id + ")");
		}
	}

	public void updateSettings() {
		List<BaseSetting<?>> list = setting.getChildSettings();

		list.clear();
		list.add(HeaderSetting.createText("Transaktionen der letzten 30 Tage").center());

		// Add transactions count
		list.add(HeaderSetting.create("§r"));
		list.add(HeaderSetting.create("Insgesamt " + (transactions.size() == 1 ? "eine Transaktion" : transactions.size() + " Transaktionen")));

		// Add transactions
		List<Transaction> transactions = new ArrayList<>(this.transactions);
		for (Transaction t : transactions) {
			if (t.recipientname == null)
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

		if (!Laby4Util.isSettingOpened(setting))
			return;

		ScreenNavigationElement element = Reflection.get(Laby4Util.getActivity(), "element");
		LabyModActivity activity = (LabyModActivity) element.getScreen();
		ScreenInstance instance = Reflection.get(activity.getActiveTab(), "instance");
		SettingsActivity settingsActivity = (SettingsActivity) instance;
		Laby.labyAPI().minecraft().executeOnRenderThread(settingsActivity::reload);
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