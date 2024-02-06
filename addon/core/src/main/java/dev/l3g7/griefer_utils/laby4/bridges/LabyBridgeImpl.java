/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.bridges;

import dev.l3g7.griefer_utils.api.BugReporter;
import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Pair;
import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.laby4.Main;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Event;
import net.labymod.api.event.LabyEvent;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.session.SessionUpdateEvent;
import net.labymod.api.event.method.SubscribeMethod;
import net.labymod.api.models.OperatingSystem;
import net.labymod.api.models.addon.info.InstalledAddonInfo;
import net.labymod.api.notification.Notification;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.tab.NameHistoryActivity;
import net.labymod.core.main.LabyMod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.function.BiFunction;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

@Bridge
@Singleton
public class LabyBridgeImpl implements LabyBridge {

	@Override
	public boolean obfuscated() {
		return !Laby.labyAPI().labyModLoader().isAddonDevelopmentEnvironment();
	}

	@Override
	public boolean forge() {
		return Laby.labyAPI().addonService().getAddon("labyforge").isPresent();
	}

	@Override
	public String addonVersion() {
		return Main.getAddon().info().getVersion();
	}

	@Override
	public float partialTicks() {
		return Laby.labyAPI().minecraft().getPartialTicks();
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
		return Notification.builder().type(Notification.Type.ADVANCEMENT)
			.title(Component.text(title))
			.text(Component.text(message))
			.icon(Icon.sprite(ResourceLocation.create("griefer_utils", "icons/icon.png"), 0, 0, 128, 128, 128, 128));
	}

	@Override
	public void displayInChat(String message) {
		Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(message);
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
	public void onJoin(Runnable callback) {
		register(ServerJoinEvent.class, v -> callback.run());
	}

	@Override
	public void onQuit(Runnable callback) {
		register(ServerDisconnectEvent.class, v -> callback.run());
	}

	@Override
	public void onAccountSwitch(Runnable callback) {
		register(SessionUpdateEvent.class, v -> callback.run());
	}

	@Override
	public void onMessageSend(Predicate<String> callback) {
		register(ChatMessageSendEvent.class, v -> v.setCancelled(callback.test(v.getMessage())));
	}

	@Override
	public void onMessageModify(BiFunction<Object, Object, Object> callback) {
		register(ChatReceiveEvent.class, v -> {
			Object newMsg = callback.apply(v.message(), v.message());
			if (newMsg != null)
				v.setMessage((Component) newMsg);
		});
	}

	@Override
	public boolean trySendMessage(String message) {
		ChatMessageSendEvent event = new ChatMessageSendEvent(message, false);
		Laby.labyAPI().eventBus().fire(event);
		return event.isCancelled();
	}

	@Override
	public HeaderSetting createLaby3DropDownPadding() {
		return null;
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
		Laby.labyAPI().minecraft().minecraftWindow().displayScreen(activity);
	}

	public static <T> void register(Class<T> event, Consumer<T> callback) {
		Laby.labyAPI().eventBus().registry().register(new SubscribeMethod() {
			@Override
			public void invoke(Event event) {
				callback.accept(c(event));
			}

			@Override
			public LabyEvent getLabyEvent() {
				return event.getAnnotation(LabyEvent.class);
			}

			public ClassLoader getClassLoader() {return null;}

			public InstalledAddonInfo getAddon() {return null;}

			public Object getListener() {return labyBridge;}

			public byte getPriority() {return 127;}

			public Method getMethod() {return null;}

			public @NotNull Class<?> getEventType() {return event;}

			public boolean isInClassLoader(ClassLoader other) {return true;}

			public SubscribeMethod copy(Object newListener) {return null;}
		});
	}

}
