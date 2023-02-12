/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.chat.command_pie_menu;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.user.gui.UserActionGui;
import net.labymod.user.util.UserActionEntry;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static net.labymod.main.LabyMod.getSettings;
import static net.labymod.user.util.UserActionEntry.EnumActionType.RUN_COMMAND;

public class PieMenu {

	private boolean wasPlayerMenuEnabled;
	private boolean wasAnimationEnabled;

	public void open(boolean animation, SettingsElement entryContainer) {
		wasPlayerMenuEnabled = getSettings().playerMenu;
		wasAnimationEnabled = getSettings().playerMenuAnimation;
		getSettings().playerMenu = false;
		getSettings().playerMenuAnimation = true;

		gui().open(player());
		if (!animation)
			Reflection.set(gui(), System.currentTimeMillis() - 2572, "selectionStarted");

		List<UserActionEntry> actionEntries = Reflection.get(gui(), "actionEntries");
		actionEntries.clear();

		for (SettingsElement element : entryContainer.getSubSettings().getElements()) {
			if (!(element instanceof PieEntryDisplaySetting))
				continue;

			PieEntryDisplaySetting entry = (PieEntryDisplaySetting) element;
			String cb = entry.cityBuild.get().getDisplayName();
			String srv = MinecraftUtil.getServerFromScoreboard();
			if (cb.equals("Jeder CB") || srv.equals(cb))
				actionEntries.add(new UserActionEntry(entry.name.get(), RUN_COMMAND, entry.command.get(), null));
		}
	}

	public void close() {
		UserActionEntry actionToExecute = Reflection.get(gui(),"actionToExecute");
		if (actionToExecute != null)
			actionToExecute.execute(null, player(), new NetworkPlayerInfo(new GameProfile(UUID.randomUUID(), "")));
		LabyMod.getSettings().playerMenu = wasPlayerMenuEnabled;
		LabyMod.getSettings().playerMenuAnimation = wasAnimationEnabled;
		gui().close();
	}

	private UserActionGui gui() {
		return LabyMod.getInstance().getUserManager().getUserActionGui();
	}

}
