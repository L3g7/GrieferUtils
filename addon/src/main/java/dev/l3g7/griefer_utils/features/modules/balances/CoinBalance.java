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

package dev.l3g7.griefer_utils.features.modules.balances;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.features.Module;
import net.labymod.settings.elements.ControlElement;

@Singleton
public class CoinBalance extends Module {

	private static long coins = -1;

	public CoinBalance() {
		super("Kontostand", "Zeigt den Kontostand an.", "coins", new ControlElement.IconData("griefer_utils/icons/coin_pile.png"));
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (event.channel.equals("coins"))
			coins = event.payload.getAsJsonObject().get("amount").getAsLong();
	}

	@Override
	public String[] getValues() {
		if (coins == -1)
			return getDefaultValues();

		return new String[] {Constants.DECIMAL_FORMAT_98.format(coins) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] {"?"};
	}

}