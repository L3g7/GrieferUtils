/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.settings.MainPage;
import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

/**
 * description missing.
 */
public class Main extends LabyModAddon {

	@Override
	public void onEnable() {
		if (!LabyModCoreMod.isForge())
			return;

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent e)  {
		for (NetworkPlayerInfo networkPlayerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
			if("AbgegrieftHD".equals(networkPlayerInfo.getGameProfile().getName())) {
				System.out.println("-----");
				System.out.println(networkPlayerInfo.getGameProfile());
				System.out.println(networkPlayerInfo.getResponseTime());
				System.out.println(networkPlayerInfo.getGameType());
				System.out.println(networkPlayerInfo.hasLocationSkin());
				System.out.println(networkPlayerInfo.getSkinType());
				System.out.println(networkPlayerInfo.getLocationSkin());
				System.out.println(networkPlayerInfo.getLocationCape());
				System.out.println(networkPlayerInfo.getPlayerTeam());
				System.out.println(networkPlayerInfo.getDisplayName());
				System.out.println(networkPlayerInfo.func_178835_l());
				System.out.println(networkPlayerInfo.func_178860_m());
				System.out.println(networkPlayerInfo.func_178847_n());
				System.out.println(networkPlayerInfo.func_178858_o());
				System.out.println(networkPlayerInfo.func_178855_p());
			}
		}
	}

	@Override
	public void loadConfig() {}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		if (!LabyModCoreMod.isForge())
			return;

		list.addAll(MainPage.settings);
	}

}
