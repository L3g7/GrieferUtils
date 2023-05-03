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

package dev.l3g7.griefer_utils.features.world;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjgl.opengl.Display;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.invoke;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.send;

/**
 * Automatically sprints when walking.
 */
@Singleton
public class AutoPortal extends Feature {

	private final BooleanSetting join = new BooleanSetting()
		.name("/portal beim Start")
		.description("Betritt automatisch GrieferGames, sobald Minecraft gestartet wurde.")
		.icon("portal");

	private final BooleanSetting maximize = new BooleanSetting()
		.name("Automatisch maximieren")
		.description("Ob Minecraft nach dem Starten automatisch maximiert werden soll.")
		.icon("maximize");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("AutoPortal")
		.description("Betritt automatisch den Portalraum.")
		.icon("portal")
		.subSettings(join, maximize);

	public void init() {
		super.init();
		if (Platform.isWindows())
			Native.register("user32");
		else
			maximize.name("§c§o§m" + maximize.getDisplayName())
				.description("§c§oMaximierung ist für " + System.getProperty("os.name") + " nicht implementiert.")
				.callback(v -> { if (v) maximize.set(false); });
	}

	@EventListener
	public void onServerJoin(ServerJoinEvent event) {
		if (event.data.getIp().toLowerCase().contains("griefergames"))
			send("/portal");
	}

	@OnStartupComplete
	public void onStartupComplete() {
		if (!isEnabled())
			return;

		if (join.get())
			mc().displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc(), new ServerData("GrieferGames", "griefergames.net", false)));

		if (maximize.get()) {
			if (Platform.isWindows()) {
				// Source: com.sun.jna.platform.WindowUtils.W32WindowUtils.getHWnd(Component)
				HWND hwnd = new HWND(new Pointer(invoke(Reflection.invoke(Display.class, "getImplementation"), "getHwnd")));
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
