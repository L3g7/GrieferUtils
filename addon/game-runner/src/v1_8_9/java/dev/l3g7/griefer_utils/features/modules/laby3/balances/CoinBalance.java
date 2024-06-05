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

package dev.l3g7.griefer_utils.features.modules.laby3.balances;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.network.play.server.S3EPacketTeams;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
@ExclusiveTo(LABY_3)
public class CoinBalance extends Laby3Module {

	private static double coins = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kontostand")
		.description("Zeigt den Kontostand an.")
		.icon("coin_pile");

	@EventListener(triggerWhenDisabled = true)
	public void onPacket(PacketEvent.PacketReceiveEvent<S3EPacketTeams> event) {
		if (world() == null)
			return;

		if (!event.packet.getName().equals("money_value") || event.packet.getAction() != 2 || MinecraftUtil.getServerFromScoreboard().equals("Portal"))
			return;

		String money = event.packet.getPrefix();
		if (!money.endsWith("$")) // Still loading
			return;

		money = money.substring(0, money.length() - 1).replace(".", "").replace(",", ".");
		coins = Double.parseDouble(money);
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
