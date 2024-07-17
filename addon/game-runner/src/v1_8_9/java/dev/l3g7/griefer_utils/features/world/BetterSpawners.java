/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.ItemUseEvent;
import dev.l3g7.griefer_utils.core.events.TileEntityDataSetEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.WorldBlockOverlayRenderer.RenderObjectGenerator;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class BetterSpawners extends Feature implements RenderObjectGenerator {

	private BlockPos lastClickedSpawner = null;
	private BlockPos lastOpenedSpawner = null;

	private final SwitchSetting spawnerWithHeldItemFix = SwitchSetting.create()
		.name("Spawner mit Item öffnen")
		.description("Ermöglicht das Öffnen von Spawnern auf öffentlichen Grundstücken, auch wenn man ein Item / einen Block in der Hand hält.")
		.icon("spawner");

	public final SwitchSetting hideMobPreview = SwitchSetting.create()
		.name("Entity verstecken")
		.description("Ob die kleine, sich drehende Vorschau des zu spawnenden Entities versteckt werden soll.")
		.icon("blindness");

	public final SwitchSetting hideParticles = SwitchSetting.create()
		.name("Partikel verstecken")
		.description("Ob die Partikel vom Spawner versteckt werden sollen.")
		.icon("green_particle");

	private final NumberSetting range = NumberSetting.create()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Icons angezeigt werden."
			+ "\n(-1 ist unendlich)")
		.defaultValue(-1)
		.min(-1)
		.icon(Items.compass);

	private final SwitchSetting showSpawnerIcons = SwitchSetting.create()
		.name("Spawner-Icons anzeigen")
		.description("Zeigt an Spawnern ein Icon des Mobs an, das gespawnt wird.")
		.icon("mob_icons/outlined_minecraft/silverfish")
		.subSettings(range);

	private final SwitchSetting markTriggeredSpawners = SwitchSetting.create()
		.name("Aktivierte Spawner markieren")
		.description("Markiert Spawner, die von der Position des Spielers aktiviert werden.")
		.icon("light_bulb");

	private final SwitchSetting markLastOpenedSpawner = SwitchSetting.create()
		.name("Zuletzt geöffneten Spawner markieren")
		.description("Markiert den Spawner, der als letztes geöffnet wurde.")
		.icon("spawner");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawner verbessern")
		.description("Verbessert Spawner.")
		.icon("spawner")
		.subSettings(spawnerWithHeldItemFix, showSpawnerIcons, markTriggeredSpawners, markLastOpenedSpawner, HeaderSetting.create(), hideMobPreview, hideParticles);

	@Override
	public void init() {
		super.init();
		WorldBlockOverlayRenderer.registerRenderObjectGenerator(this);
	}

	// Spawner with held item fix

	@EventListener
	private void onPacketSend(ItemUseEvent.Pre event) {
		if (!ServerCheck.isOnGrieferGames() || player().isSneaking())
			return;

		if (event.stack != player().getHeldItem())
			// Packet probably was sent by a mod / addon
			return;

		Block clickedBlock = world().getBlockState(event.pos).getBlock();
		if (clickedBlock != Blocks.mob_spawner || MinecraftUtil.isInFarmwelt())
			return;

		if (event.stack == null || spawnerWithHeldItemFix.get())
			lastClickedSpawner = event.pos;

		if (!spawnerWithHeldItemFix.get() || event.stack == null)
			return;

		if (FileProvider.getBridge(TempItemSaverBridge.class).isProtected(event.stack)) {
			labyBridge.notify("§cItemSaver", "§cDas Item in deiner Hand ist im ItemSaver!");
			return;
		}

		event.cancel();

		click(event.stack);
		mc().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(event.pos, event.side.getIndex(), null, event.hitX, event.hitY, event.hitZ));
		click(null);
	}

	private void click(ItemStack itemstack) {
		short transactionID = player().openContainer.getNextTransactionID(player().inventory);
		int slotId = player().inventory.currentItem + 36;
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(0, slotId, 0, 0, itemstack, transactionID));
	}

	// Mark last opened spawner

	@EventListener
	private void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (title.startsWith("§6Spawner - Lager") && lastClickedSpawner != null) {
			lastOpenedSpawner = lastClickedSpawner;
			lastClickedSpawner = null;
		}
	}

	@EventListener(triggerWhenDisabled = true)
	private void onServerSwitch(ServerSwitchEvent event) {
		lastOpenedSpawner = lastClickedSpawner = null;
	}

	// Show spawner icons & show triggered spawners

	@Override
	public RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world) {
		if (state.getBlock() != Blocks.mob_spawner)
			return null;

		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityMobSpawner))
			return null;

		return new SpawnerRenderObject(tileEntity);
	}

	@EventListener
	private void onTileEntityDataSet(TileEntityDataSetEvent event) {
		if (!(event.tileEntity instanceof TileEntityMobSpawner))
			return;

		ChunkCoordIntPair pair = new ChunkCoordIntPair(event.tileEntity.getPos().getX() >> 4, event.tileEntity.getPos().getZ() >> 4);
		WorldBlockOverlayRenderer.getRenderObjectsForChunk(pair).put(event.tileEntity.getPos(), new SpawnerRenderObject(event.tileEntity));
	}

	// Hide mobs

	@Mixin(TileEntityMobSpawnerRenderer.class)
	private static class MixinTileEntityMobSpawnerRenderer {

		@Unique
		private static final BetterSpawners BETTER_SPAWNERS = FileProvider.getSingleton(BetterSpawners.class);

		@Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityMobSpawner;DDDFI)V", at = @At("HEAD"), cancellable = true)
		private void injectRenderTileEntityAt(TileEntityMobSpawner te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
			if (BETTER_SPAWNERS.isEnabled() && BETTER_SPAWNERS.hideMobPreview.get())
				ci.cancel();
		}

	}

	// Hide particles

	@Mixin(MobSpawnerBaseLogic.class)
	private static class MixinMobSpawnerBaseLogic {

		@Unique
		private static final BetterSpawners BETTER_SPAWNERS = FileProvider.getSingleton(BetterSpawners.class);

		@Redirect(method = "updateSpawner", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
		private void redirectSpawnParticle(World instance, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] p_175688_14_) {
			if (!BETTER_SPAWNERS.isEnabled() || !BETTER_SPAWNERS.hideParticles.get())
				instance.spawnParticle(particleType, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
		}

	}

	private class SpawnerRenderObject extends RenderObject {

		private final ResourceLocation resourceLocation;
		private final MobSpawnerBaseLogic mobSpawnerBaseLogic;

		private SpawnerRenderObject(TileEntity tileEntity) {
			super(BetterSpawners.this);

			TileEntityMobSpawner spawner = (TileEntityMobSpawner) tileEntity;
			this.mobSpawnerBaseLogic = spawner.getSpawnerBaseLogic();
			Entity entity = spawner.getSpawnerBaseLogic().func_180612_a(world());
			if (entity == null) {
				resourceLocation = new ResourceLocation("missing_entity_texture_null");
				return;
			}

			resourceLocation = new ResourceLocation("griefer_utils", "icons/mob_icons/outlined_minecraft/" + EntityList.getEntityString(entity).toLowerCase() + ".png");
		}

		@Override
		public void render(BlockPos pos, float partialTicks, int chunksFromPlayer) {
			if (markTriggeredSpawners.get() && (boolean) Reflection.invoke(mobSpawnerBaseLogic, "isActivated")) {
				GlStateManager.disableDepth();
				RenderUtil.drawBoxOutlines(new AxisAlignedBB(pos, pos.add(1, 1, 1)), new Color(0xFFFF00), 3);
				GlStateManager.enableDepth();
			}

			if (markLastOpenedSpawner.get() && pos.equals(lastOpenedSpawner)) {
				GlStateManager.disableDepth();
				RenderUtil.drawFilledBox(new AxisAlignedBB(pos, pos.add(1, 1, 1)), new Color(0x6000FF00, true), false);
				GlStateManager.enableDepth();
			}

			if (!showSpawnerIcons.get() || (range.get() != -1 && range.get() < chunksFromPlayer))
				return;

			prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

			GlStateManager.translate(0, 0.75, 0);
			GlStateManager.scale(1, -1, 1);

			GlStateManager.color(1, 1, 1, 1);
			mc().getTextureManager().bindTexture(resourceLocation);

			for (int i = 0; i < 4; i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(-0.25f, 0, 0.51f);
				GlStateManager.scale(1/512f, 1/512f, 1/512f);
				DrawUtils.drawTexturedModalRect(0, 0, 256, 256);
				GlStateManager.popMatrix();
				GlStateManager.rotate(90, 0, 1, 0);
			}

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.26f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			DrawUtils.drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, -1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.76f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			DrawUtils.drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}

	}

}
