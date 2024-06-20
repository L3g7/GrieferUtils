/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.Laby;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjgl.opengl.Display;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.send;

/**
 * Automatically sprints when walking.
 */
@Singleton
public class AutoPortal extends Feature {

	private final SwitchSetting join = SwitchSetting.create()
		.name("/portal beim Start")
		.description("Betritt automatisch GrieferGames, sobald Minecraft gestartet wurde.")
		.icon("portal");

	private final SwitchSetting maximize = SwitchSetting.create()
		.name("Automatisch maximieren")
		.description("Ob Minecraft nach dem Starten automatisch maximiert werden soll.")
		.icon("maximize");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch /portal")
		.description("Betritt automatisch den Portalraum.")
		.icon("portal")
		.subSettings(join, maximize);

	public void init() {
		super.init();
		if (Platform.isWindows())
			Native.register("user32");
		else
			maximize.name("§c§o§m" + maximize.name())
				.description("§c§oMaximierung ist für " + System.getProperty("os.name") + " nicht implementiert.")
				.callback(v -> { if (v) maximize.set(false); });
	}

	@EventListener(priority = Priority.HIGH)
	public void onServerJoin(ServerEvent.GrieferGamesJoinEvent event) {
		send("/portal");
	}

	@OnStartupComplete
	public void onStartupComplete() {
		if (!isEnabled())
			return;

		if (join.get())
			mc().addScheduledTask(() -> mc().displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc(), new ServerData("GrieferGames", "griefergames.net", false))));

		if (maximize.get()) {
			if (Platform.isWindows()) {
				long handle = LABY_4.isActive()
					? Laby.gfx().backend().glfwNatives().getWin32Window(Display.getWindowHandle())
					: Reflection.invoke(Reflection.invoke(Display.class, "getImplementation"), "getHwnd");

				HWND hwnd = new HWND(new Pointer(handle));
				ShowWindow(hwnd, WinUser.SW_SHOWMAXIMIZED);
				SetForegroundWindow(hwnd);
				SetActiveWindow(hwnd);
			}
		}
	}

	private static native HWND SetActiveWindow(HWND hwnd);
	private static native boolean SetForegroundWindow(HWND hwnd);
	private static native boolean ShowWindow(HWND hwnd, int nCmdShow);

}
