/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu;

import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

public class PieMenu extends dev.l3g7.griefer_utils.v1_8_9.misc.gui.guis.PieMenu {

	public void open(boolean animation, BaseSetting entryContainer) {
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		for (BaseSetting pageElement : ((BaseSetting<?>) entryContainer).getSubSettings()) {
			if (!(pageElement instanceof PieMenuPageSetting))
				continue;

			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (BaseSetting element : ((BaseSetting<?>) pageElement).getSubSettings()) {
				if (!(element instanceof PieMenuEntrySetting))
					continue;

				PieMenuEntrySetting entry = (PieMenuEntrySetting) element;
				String cb = ((ItemStack) c(entry.citybuild.get())).getDisplayName();
				String srv = MinecraftUtil.getServerFromScoreboard();
				if (!cb.equals("Egal") && !srv.equals(cb))
					continue;

				entries.add(Pair.of(DrawUtils.createColors(entry.name.get()), () -> {
					if (!MessageEvent.MessageSendEvent.post(entry.command.get()))
						player().sendChatMessage(entry.command.get());
				}));
			}

			if (!entries.isEmpty())
				allPages.add(Pair.of(DrawUtils.createColors(((PieMenuPageSetting) pageElement).name.get()), entries));
		}

		open(animation, allPages);
	}

}