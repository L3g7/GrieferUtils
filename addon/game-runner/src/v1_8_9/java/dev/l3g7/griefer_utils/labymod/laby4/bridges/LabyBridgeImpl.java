/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.bridges;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.mapping.Mapping;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.labymod.laby4.Main;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.network.playerinfo.PlayerInfoUpdateEvent;
import net.labymod.api.models.OperatingSystem;
import net.labymod.api.notification.Notification;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.ChatInputOverlay;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.tab.NameHistoryActivity;
import net.labymod.core.main.LabyMod;
import net.labymod.v1_8_9.client.player.VersionedNetworkPlayerInfo;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.OBFUSCATED;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.UNOBFUSCATED;
import static net.labymod.api.Laby.labyAPI;
import static net.labymod.api.event.client.network.playerinfo.PlayerInfoUpdateEvent.UpdateType.DISPLAY_NAME;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class LabyBridgeImpl implements LabyBridge {

	@Override
	public boolean obfuscated() {
		return false;
	}

	@Override
	public boolean inDevEnv() {
		return labyAPI().labyModLoader().isAddonDevelopmentEnvironment();
	}

	@Override
	public Mapping activeMapping() {
		return obfuscated() ? OBFUSCATED : UNOBFUSCATED;
	}

	@Override
	public boolean forge() {
		return labyAPI().addonService().getAddon("labyforge").isPresent();
	}

	@Override
	public String addonVersion() {
		return Main.getAddon().info().getVersion();
	}

	@Override
	public boolean isBeta() {
		JsonObject addonJson = Streams.parse(new JsonReader(new InputStreamReader(FileProvider.getData("addon.json")))).getAsJsonObject();
		return !addonJson.has("beta") || addonJson.get("beta").getAsBoolean();
	}

	@Override
	public float partialTicks() {
		return labyAPI().minecraft().getPartialTicks();
	}

	@Override
	public int chatButtonWidth() {
		if (!(Laby4Util.getActivity() instanceof ChatInputOverlay))
			return 0;

		return (int) labyAPI().chatProvider().chatInputService().getButtonWidth() - 1;
	}

	@Override
	public void notify(String title, String message, int ms) {
		createNotification(title, message)
			.duration(ms)
			.buildAndPush();
	}

	@Override
	public void notifyError(String message) {
		createNotification("§c§lFehler ⚠", "§c" + message)
			.duration(15_000)
			.addButton(Notification.NotificationButton.of(Component.text("Zum Discord"), () -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")))
			.buildAndPush();
	}

	private Notification.Builder createNotification(String title, String message) {
		return Notification.builder().type(Notification.Type.SYSTEM)
			.title(Component.text(title))
			.text(Component.text(message))
			.icon(Icon.sprite(ResourceLocation.create("griefer_utils", "icons/high_res/icon.png"), 0, 0, 128, 128, 128, 128));
	}

	@Override
	public void displayInChat(String message) {
		labyAPI().minecraft().chatExecutor().displayClientMessage(message);
	}

	@Override
	public void openWebsite(String url) {
		OperatingSystem.getPlatform().openUrl(url);
	}

	@Override
	public boolean openFile(File file) {
		try {
			return OperatingSystem.getPlatform().launchUrlProcess(file.toURI().toURL());
		} catch (MalformedURLException e) {
			BugReporter.reportError(e);
			return false;
		}
	}

	@Override
	public void copyText(String text) {
		labyAPI().minecraft().setClipboard(text);
	}

	@Override
	public boolean trySendMessage(String message) {
		ChatMessageSendEvent event = new ChatMessageSendEvent(message, false);
		labyAPI().eventBus().fire(event);
		return event.isCancelled();
	}

	@Override
	public Pair<String, String> getCachedTexture(UUID uuid) {
		ResourceLocation location = Icon.head(uuid).getResourceLocation();
		return location == null ? null : new Pair<>(location.getNamespace(), location.getPath());
	}

	@Override
	public void openNameHistory(String name) {
		NameHistoryActivity activity = LabyMod.references().nameHistoryActivity();
		activity.scheduleQuery(name);
		labyAPI().minecraft().minecraftWindow().displayScreen(activity);
	}

	@Override
	public void syncTabList(NetworkPlayerInfo info) {
		Laby.fireEvent(new PlayerInfoUpdateEvent(new VersionedNetworkPlayerInfo(info), DISPLAY_NAME));
	}

}
