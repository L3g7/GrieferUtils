/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os.impl.linux;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.os.OS;
import dev.l3g7.griefer_utils.core.api.misc.os.OSFallback;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.api.util.io.ReadOperation;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LINUX;

@Bridge
@Singleton
@ExclusiveTo(LINUX)
public class Linux implements OS {

	private static final String[] KNOWN_SAFE_DES = new String[]{
		"xfce", "lxde", "lxqt", "mate",
		"kde", // Plasma
		"dde", "deepin", // Deepin
		"cosmic", // PopOS
	};

	private static final String[] KNOWN_SAFE_DE_BASES = new String[]{
		"gnome", "unity", // Ubuntu
		"cinnamon", // Mint
	};

	private final Runnable maximizeImpl;

	public Linux() {
		String sessionType = System.getenv("XDG_SESSION_TYPE");
		boolean isX11OrXWayland = GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11;
		String desktop = System.getenv("XDG_CURRENT_DESKTOP").toLowerCase();

		Runnable fallbackImpl = FileProvider.getSingleton(OSFallback.class)::maximizeWindow;

		if (desktop.equals("hyprland"))
			maximizeImpl = this::maximizeWindowHyprland;
		else {
			maximizeImpl = fallbackImpl;
			// Check if WM / DE is known to work
			if (isX11OrXWayland) {
				for (String knownSafeDE : KNOWN_SAFE_DES)
					if (desktop.equals(knownSafeDE))
						return;

				for (String knownSafeDE : KNOWN_SAFE_DE_BASES)
					if (desktop.contains(knownSafeDE))
						return;
			}

			BugReporter.reportError(Util.elevate(null,
				"Unsupported DM: " + sessionType + " / " + desktop + " / " + GLFW.glfwGetPlatform()));
		}
	}

	@Override
	public void maximizeWindow() {
		maximizeImpl.run();
	}

	@Override
	public @Nullable File chooseImageFile() {
		List<String> uris = XDGPortalImageFileChooser.chooseImageFile("Wähle ein Bild aus");
		return uris.isEmpty() ? null : new File(URI.create(uris.getFirst()));
	}

	private void maximizeWindowHyprland() {
		try {
			String res = shell(true, "hyprctl systeminfo | grep -i configProvider").asString().trim();
			boolean isLua = res.equals("configProvider: lua");

			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			if (!pid.matches("^\\d+$"))
				throw new IllegalStateException("Invalid PID: '" + pid + "'");

			JsonObject activeWindow = shell("hyprctl activewindow -j").asJsonObject();
			String activeWindowPid = activeWindow.has("pid") ? activeWindow.get("pid").getAsString() : pid;

			// Check if Minecraft and active window are in the same workspace
			boolean isSameWorkspace = StreamSupport.stream(shell("hyprctl clients -j").asJsonArray().spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.filter(e -> e.get("pid").getAsString().equals(pid)
					|| e.get("pid").getAsString().equals(activeWindowPid))
				.map(e -> e.get("workspace").getAsJsonObject().get("id").getAsInt())
				.distinct().limit(2).count() <= 1;

			// Only focus if on different workspaces; otherwise focusing the previous window would move Minecraft back
			boolean shouldFocusBack = !isSameWorkspace;

			List<String> commands;
			if (isLua) {
				commands = new ArrayList<>(Collections.singletonList(
					"dispatch hl.dsp.window.fullscreen({ mode = \"maximized\", action = \"set\", window = \"pid:" + pid + "\" })"
				));
				if (shouldFocusBack)
					commands.add("dispatch hl.dsp.focus({ window = \"pid:" + activeWindowPid + "\" })");
			} else {
				commands = new ArrayList<>(Arrays.asList(
					"dispatch focuswindow pid:" + pid,
					"dispatch fullscreen 1 set"
				));
				if (shouldFocusBack)
					commands.add("dispatch focuswindow pid:" + activeWindowPid);
			}

			String dispatchRes = shell("hyprctl --batch \"" + Strings.join(commands, " ; ").replace("\"", "\\\"") + "\"").asString();
			dispatchRes = dispatchRes.trim().replaceAll("\\s+", " ");
			String expected = String.join(" ", Collections.nCopies(commands.size(), "ok"));
			if (!dispatchRes.equalsIgnoreCase(expected))
				throw new IllegalStateException("Response: '" + dispatchRes + "'");
		} catch (InterruptedException | TimeoutException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ReadOperation shell(String command) throws InterruptedException, TimeoutException, IOException {
		return shell(false, command);
	}

	private static ReadOperation shell(boolean allowFailure, String command) throws InterruptedException, TimeoutException, IOException {
		var proc = new ProcessBuilder()
			.command("sh", "-c", command)
			.start();

		if (!proc.waitFor(1, TimeUnit.SECONDS))
			throw new TimeoutException();

		if (!allowFailure && proc.exitValue() != 0)
			throw new IllegalStateException("Exit value " + proc.exitValue());

		return IO.read(proc.getInputStream());
	}

}
