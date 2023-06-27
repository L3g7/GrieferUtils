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

package dev.l3g7.griefer_utils.features.player.player_list;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;

import static net.labymod.utils.ModColor.GREEN;

/**
 * An indicator for players on verbvllert_'s trusted list.
 */
@Singleton
public class TrustedList extends PlayerList {

	public TrustedList() {
		super("Trusted MM-Liste", "Markiert Spieler in verbvllert_s Trusted-MM-Liste.", "✰", "green_scroll", "Eigene Trusted", GREEN, 5, "§a§lTrusted", "https://grieferutils.l3g7.dev/v2/trusted_mms");
	}

}
