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

package dev.l3g7.griefer_utils.features.uncategorized.debug.wiki;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason.NOT_NEEDED;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@ExclusiveTo(value = LABY_3, reason = NOT_NEEDED)
public class EnchantmentRenderer {

	@OnEnable
	private static void onEnable() {
		if (Boolean.parseBoolean(System.getProperty("addWiki", "false"))) {
			Runnable runnable = () -> MinecraftForge.EVENT_BUS.register(new EnchantmentRenderer());
			runnable.run();
		}
	}

	private static ItemRenderer itemRenderer;
	private static long end = 0;
	private static long timer = 0;
	private static int seconds = 0;
	private static boolean ench = false;

	private static final NumberSetting scale = NumberSetting.create().name("Größe").defaultValue(640);

	public static final KeySetting enabled = KeySetting.create().name("Itemrenderer").pressCallback(b -> {
		if (b)
			return;

		if (player() == null)
			return;

		if (player().getHeldItem() == null) {
			display("Bitte halte ein Item in der Hand!");
			return;
		}

		if (itemRenderer != null) {
			display("Es wird bereits ein Item gerendert!");
			return;
		}
		NBTTagCompound tag = player().getHeldItem().getTagCompound();
		ench = tag != null && tag.hasKey("ench");
		if (ench && Minecraft.getDebugFPS() > 25) {
			Minecraft.getMinecraft().gameSettings.limitFramerate = 25;
			display("FPS wurden runtergestellt, bitte warte kurz");
			return;
		}

		if (ench)
			display("Started");
		itemRenderer = new ItemRenderer(player().getHeldItem(), scale.get());
	}).subSettings(scale).icon("enchanted_book");

	@SubscribeEvent(priority= EventPriority.HIGHEST)
	public void onFrameStart(TickEvent.RenderTickEvent e) {
		if (e.phase != TickEvent.Phase.START || itemRenderer == null)
			return;

		new File(Minecraft.getMinecraft().mcDataDir, "rendered_enchantments/").mkdirs();

		if (!ench) {
			BufferedImage img = itemRenderer.grabFrame();
			try {
				ImageIO.write(img, "png", new File(Minecraft.getMinecraft().mcDataDir, "rendered_enchantments/" + itemRenderer.stack.getDisplayName().toLowerCase().replace(' ', '_') + ".png"));
			} catch (IOException ex) {
				try {
					ImageIO.write(img, "png", new File(Minecraft.getMinecraft().mcDataDir, "rendered_enchantments/" + UUID.randomUUID().toString() + ".png"));
				} catch (IOException ex2) {
					itemRenderer = null;
					throw new RuntimeException(ex);
				}
			}
			itemRenderer = null;
			display("Done");
			return;
		}

		if (end == 0) {
			timer = seconds = 0;
			long l = System.currentTimeMillis();
			end = l + (l % 14619) + 14619;
			try {
				Thread.sleep(l % 14619);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		} else if (end <= System.currentTimeMillis()) {
			end = 0;
			itemRenderer.finish();
			itemRenderer = null;
			display("Done");
			return;
		}

		if (timer++ >= 25) {
			display("Sekunde " + (++seconds) + " vorbei.");
			timer = 0;
		}
		itemRenderer.addFrame();
	}

}
