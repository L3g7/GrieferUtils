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

package dev.l3g7.griefer_utils.features.world.better_hopper;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class BetterHopper extends Feature {

	private static final String ENABLE_VISUALISATION_NBT = "{id:\"minecraft:ender_eye\",Count:1b,tag:{display:{Lore:[0:\"§7Zeigt für §e%d Sekunden§7 %s und den Sammelradius optisch an.\",1:\"§7 \",2:\"§7Klicke, um die Anzeige zu starten.\"],Name:\"§6Optische Anzeige\"}},Damage:0s}";

	private final Map<BlockPos, EntityItem> filteredConnections = new HashMap<>();
	private BlockPos hopper;
	private BlockPos mainConnection;
	private int borderSize;
	private long displayEnd = -1;
	private BlockyRenderSphere blockyRenderSphere = null;

	private final BooleanSetting fillBoxes = new BooleanSetting()
		.name("Anzeigeboxen füllen")
		.description("Ob die Boxen der Anzeige gefüllt werden sollen.")
		.defaultValue(true)
		.icon(ItemUtil.createItem(Blocks.wool, 14, false));

	private final NumberSetting displayTime = new NumberSetting()
		.name("Anzeigedauer")
		.description("Wie lange wie optische Anzeige aktiv bleiben soll, in Sekunden.")
		.icon("hourglass")
		.defaultValue(10);

	private final BooleanSetting betterVisualisation = new BooleanSetting()
		.name("Bessere optische Trichter-Anzeige")
		.description("Ersetzt die Partikel der optischen Trichter Anzeige durch Boxen / Linien.")
		.icon(Material.EYE_OF_ENDER)
		.subSettings(displayTime, fillBoxes);

	private final BooleanSetting showRange = new BooleanSetting()
		.name("Trichterreichweite anzeigen")
		.description("Zeigt die Trichterreichtweite an.")
		.icon("ruler");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Bessere Trichter")
		.description("Verbessert Trichter.")
		.icon(Material.HOPPER)
		.subSettings(betterVisualisation, showRange);

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent event) {
		if (!(event.packet instanceof C0EPacketClickWindow) || !(mc().currentScreen instanceof GuiChest))
			return;

		C0EPacketClickWindow packet = (C0EPacketClickWindow) event.packet;
		if (packet.getMode() == 3)
			return;

		int slot = packet.getSlotId();

		IInventory inv = Reflection.get(mc().currentScreen, "lowerChestInventory");
		if (inv.getName().equals("§6Trichter-Mehrfach-Verbindungen")) {
			if (slot == 53) {
				blockyRenderSphere = BlockyRenderSphere.getSphere(hopper);
				return;
			}

			if ((slot != 52 && slot != 50))
				return;

			for (int i = 0; i < 44; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack == null || stack.getDisplayName().equals("§7 "))
					continue;

				BlockPos pos = getBlockPos(stack);
				EntityItem entityItem = new EntityItem(world(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
				filteredConnections.put(pos, entityItem);
			}

			if (slot == 52) {
				displayEnd = System.currentTimeMillis() + displayTime.get() * 1000;
				mc().displayGuiScreen(null);
				event.setCanceled(true);
			}
		}

		if (!inv.getName().equals("§6Trichter-Einstellungen"))
			return;

		if (slot != 15 && (slot != 34 || !betterVisualisation.get()) && (slot != 16 || !showRange.get()))
			return;

		filteredConnections.clear();
		Container slots = ((GuiChest) mc().currentScreen).inventorySlots;
		borderSize = slots.getSlot(31).getStack().stackSize;
		hopper = getBlockPos(slots.getSlot(13).getStack());

		if (slot == 16) {
			blockyRenderSphere = BlockyRenderSphere.getSphere(hopper);
			return;
		}

		ItemStack targetStack = slots.getSlot(16).getStack();
		mainConnection = EnchantmentHelper.getEnchantments(targetStack).isEmpty() ? null : getBlockPos(targetStack);

		if (slot == 34) {
			displayEnd = System.currentTimeMillis() + displayTime.get() * 1000;
			mc().displayGuiScreen(null);
			event.setCanceled(true);
		}
	}

	private BlockPos getBlockPos(ItemStack stack) {
		String line = ItemUtil.getLore(stack).get(0);
		String blockPos = line.substring(line.indexOf("§e") + 2);
		String[] coords = blockPos.split(";");
		return new BlockPos(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
	}

	@EventListener
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (!betterVisualisation.get() || event.phase != TickEvent.Phase.START || !(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc().currentScreen, "lowerChestInventory");
		boolean isSettings = inv.getName().equals("§6Trichter-Einstellungen");

		if (!isSettings && !inv.getName().equals("§6Trichter-Mehrfach-Verbindungen"))
			return;

		int slotId = isSettings ? 34 : 52;
		ItemStack stack = ItemUtil.fromNBT(String.format(ENABLE_VISUALISATION_NBT, displayTime.get(), isSettings ? "die Verbindung" : "alle Verbindungen"));
		((GuiChest) mc().currentScreen).inventorySlots.getSlot(slotId).putStack(stack);
	}

	@EventListener
	public void onMessageReceive(ClientChatReceivedEvent event) {
		String text = event.message.getUnformattedText();
		if (text.equals("[Trichter] Der Trichter wurde erfolgreich verbunden.")
			|| text.equals("[Trichter] Der Verbindungsmodus wurde beendet.")
			|| text.equals("[Trichter] Der Startpunkt ist zu weit entfernt. Bitte starte erneut."))
			blockyRenderSphere = null;
	}

	@EventListener
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (blockyRenderSphere != null)
			blockyRenderSphere.render();

		if (displayEnd < System.currentTimeMillis()) // Visualisation
			return;

		GL11.glDisable(GL_DEPTH_TEST);

		if (mainConnection != null)
			drawConnection(mainConnection, 0xFF0000);

		renderFilteredConnections(event.partialTicks);

		AxisAlignedBB collectionBox;
		if (borderSize > 1)
			collectionBox = new AxisAlignedBB(hopper.add(borderSize, borderSize / 2, borderSize), hopper.add(-borderSize, borderSize / -2, -borderSize));
		else if (borderSize == 1)
			collectionBox = new AxisAlignedBB(hopper, hopper.add(1, 1, 1));
		else
			collectionBox = new AxisAlignedBB(hopper, hopper).expand(0.125, 0.125, 0.125).offset(0.5, 0.5, 0.5);

		if (fillBoxes.get())
			RenderUtil.drawFilledBox(collectionBox, new Color(0x1A880088, true), true);
		RenderUtil.drawBoxOutlines(collectionBox, new Color(0x880088), 1.5f);
		GlStateManager.enableBlend();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		GL11.glEnable(GL_DEPTH_TEST);
	}

	private void renderFilteredConnections(float partialTicks) {
		for (BlockPos blockPos : filteredConnections.keySet())
			drawConnection(blockPos, 0xFF4040);

		for (EntityItem entityItem : filteredConnections.values()) {
			Entity renderEntity =  mc().getRenderViewEntity();
			double x = entityItem.posX - renderEntity.lastTickPosX - (renderEntity.posX - renderEntity.lastTickPosX) * partialTicks;
			double y = entityItem.posY - renderEntity.lastTickPosY - (renderEntity.posY - renderEntity.lastTickPosY) * partialTicks;
			double z = entityItem.posZ - renderEntity.lastTickPosZ - (renderEntity.posZ - renderEntity.lastTickPosZ) * partialTicks;
			ItemStack stack = entityItem.getEntityItem();

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			double yaw = Math.toDegrees(MathHelper.atan2(entityItem.posX - renderEntity.posX, entityItem.posZ - renderEntity.posZ));
			GlStateManager.rotate((float) yaw, 0, 1, 0);

			double dist = Math.sqrt(Math.pow(entityItem.posX - renderEntity.posX, 2) + Math.pow(entityItem.posZ - renderEntity.posZ, 2));
			double pitch = Math.toDegrees(MathHelper.atan2(entityItem.posY - (renderEntity.posY + renderEntity.getEyeHeight()), dist));
			GlStateManager.rotate(-(float) pitch, 1, 0, 0);

			// Draw outline
			double delta = 1 /16d;
			GL11.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_ADD);
			GlStateManager.color(1, 0, 0, 1);
			GlStateManager.colorMask(true, false, false, true);
			drawStack(delta, delta, stack);
			drawStack(delta, -delta, stack);
			drawStack(-delta, delta, stack);
			drawStack(-delta, -delta, stack);
			GlStateManager.colorMask(true, true, true, true);
			GL11.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

			drawStack(0, 0, stack);
			GlStateManager.popMatrix();
		}
	}

	@SuppressWarnings("deprecation")
	private void drawStack(double shiftX, double shiftY, ItemStack stack) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(shiftX, shiftY, 0);
		mc().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.popMatrix();
	}

	private void drawConnection(BlockPos target, int color) {
		AxisAlignedBB targetBox = new AxisAlignedBB(target, target.add(1, 1, 1));
		if (fillBoxes.get())
			RenderUtil.drawFilledBox(targetBox, new Color(0x1A000000 | color, true), true);
		RenderUtil.drawBoxOutlines(targetBox, new Color(color), 1.5f);
		RenderUtil.drawLine(hopper.getX() + 0.5f, hopper.getY() + 0.5f, hopper.getZ() + 0.5f, target.getX() + 0.5f, target.getY() + 0.5f, target.getZ() + 0.5f, new Color(color), 1.5f);
	}

}