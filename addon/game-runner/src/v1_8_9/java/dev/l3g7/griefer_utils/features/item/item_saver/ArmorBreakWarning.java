/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldEvent;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * Displays a warning when equipped armor falls below the set durability.
 * (0 to disable)
 */
@Singleton
public class ArmorBreakWarning extends Feature {

	private final FontRenderer font = mc().fontRendererObj;

	private final Item[] warnItems = new Item[]{Items.iron_boots, Items.iron_leggings, Items.iron_chestplate, Items.iron_helmet};
	private ItemStack currentWarnItem = null;


	@MainElement
	private final NumberSetting threshold = NumberSetting.create()
		.name("Bei Rüstungsschaden warnen")
		.description("Zeigt eine Warnung an, sobald eine angezogene Rüstung die eingestellte Haltbarkeit unterschreitet.", "(0 zum Deaktivieren)")
		.icon("diamond_chestplate");

	@EventListener
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		currentWarnItem = null;

		// Go through armor
		ItemStack[] armor = armorInventory();
		for (int i = 0; i < armor.length; i++) {
			ItemStack stack = armor[i];

			// Check if item can get damaged
			if (stack == null || !stack.isItemStackDamageable())
				continue;

			int itemDurability = stack.getMaxDamage() - stack.getItemDamage();

			// Check if durability is less than threshold
			if (threshold.get() > itemDurability)
				currentWarnItem = new ItemStack(warnItems[i]);
		}
	}


	@EventListener
	public void onRenderWorld(RenderWorldEvent event) {
		ItemStack warnItem = this.currentWarnItem; // Avoid concurrent modifications
		if (warnItem == null)
			return;

		float scale = 2;

		int strWidth = font.getStringWidth("§cgeht kaputt!") + 18; // 16px Item + 2px padding
		float x = (screenWidth() - strWidth * scale) / 2f;
		float y = screenHeight() / 2f + 3; // 3px shift

		GlStateManager.scale(scale, scale, 0);
		GlStateManager.translate(x / scale, y / scale, 0);

		renderItem(warnItem);
		font.drawStringWithShadow("§cgeht kaputt!", 18, 4, -1);
	}

	private static void renderItem(ItemStack stack) {
		RenderItem renderItem = mc().getRenderItem();
		IBakedModel model = renderItem.getItemModelMesher().getItemModel(stack);

		mc().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		mc().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		GlStateManager.pushMatrix();

		// Transform
		Reflection.invoke(renderItem, "setupGuiTransform", 0, 0, model.isGui3d());
		model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
		GlStateManager.scale(0.5f, 0.5f, 0.5f);
		GlStateManager.translate(-0.5f, -0.5f, -0.5f);

		// Draw item
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		wr.begin(GL_QUADS, DefaultVertexFormats.ITEM);
		Reflection.invoke(renderItem, "renderQuads", wr, model.getGeneralQuads(), 0xFFFF5555, stack);

		GlStateManager.popMatrix();
		Tessellator.getInstance().draw();
	}

}
