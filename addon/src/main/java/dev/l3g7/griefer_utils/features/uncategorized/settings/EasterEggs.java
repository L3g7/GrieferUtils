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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsGroup;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import org.lwjgl.input.Keyboard;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class EasterEggs {

	private static final ModTextField textField = new ModTextField(0, null, 0, 0, 0, 0);

	private static void check(String input) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		byte[] bytes = digest.digest((input + "_griefer_utils_salt").getBytes(StandardCharsets.UTF_8));
		String hash = Base64.getEncoder().encodeToString(bytes);

		if (hash.equals("y5ujXCf0qm4hISacw31ujJfMfZgrCDlHxlqLCLGzBn/LjYU9SyEb9XLIGmvgccH7tvU3Y1JIPIDz6I8HdsQqOw=="))
			GrieferUtilsGroup.icon = GrieferUtilsGroup.icon.equals("icon") ? input : "icon";
		else
			return;

		textField.setText("");
		displayAchievement("§aEaster Egg", "Easter Egg wurde umgeschalten.");
		if (world() != null)
			mc().displayGuiScreen(null);
	}

	@EventListener
	private static void onKey(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!Keyboard.getEventKeyState())
			return;

		if (!(mc().currentScreen instanceof LabyModAddonsGui))
			return;

		List<SettingsElement> path = path();
		if (path.size() == 0)
			return;

		if (path.get(path.size() - 1) != FileProvider.getSingleton(Settings.class).getMainElement())
			return;

		String text = textField.getText();
		if (textField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey()) && !text.equals(textField.getText())) {
			try {
				check(textField.getText());
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("SHA-512 does not exist!");
			}
		}
	}

	static {
		textField.setFocused(true);
		textField.setMaxStringLength(Integer.MAX_VALUE);
	}

}
