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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.ItemUseEvent;
import dev.l3g7.griefer_utils.event.events.TileEntityDataSetEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObject;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObjectGenerator;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
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

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.utils.Material.COMPASS;

@Singleton
public class BetterSpawners extends Feature implements RenderObjectGenerator {

	private final BooleanSetting spawnerWithHeldItemFix = new BooleanSetting()
		.name("Spawner mit Item öffnen")
		.description("Ermöglicht das Öffnen von Spawnern auf öffentlichen Grundstücken, auch wenn man ein Item / einen Block in der Hand hält.")
		.icon("spawner");

	public final BooleanSetting hideMobPreview = new BooleanSetting()
		.name("Entity verstecken")
		.description("Ob die kleine, sich drehende Vorschau des zu spawnenden Entities versteckt werden soll.")
		.icon("blindness");

	public final BooleanSetting hideParticles = new BooleanSetting()
		.name("Partikel verstecken")
		.description("Ob die Partikel vom Spawner versteckt werden sollen.")
		.icon("green_particle");

	private final NumberSetting range = new NumberSetting()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Icons angezeigt werden."
			+ "\n(-1 ist unendlich)")
		.defaultValue(-1)
		.min(-1)
		.icon(COMPASS);

	private final BooleanSetting showSpawnerIcons = new BooleanSetting()
		.name("Spawner-Icons anzeigen")
		.description("Zeigt an Spawnern ein Icon des Mobs an, das gespawnt wird.")
		.icon("griefer_info/outlined_mob_icons/Silverfish")
		.subSettings(range);

	private final BooleanSetting markTriggeredSpawners = new BooleanSetting()
		.name("Aktivierte Spawner markieren")
		.description("Markiert Spawner, die von der Position des Spielers aktiviert werden.")
		.icon("light_bulb");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spawner verbessern")
		.description("Verbessert Spawner.")
		.icon("spawner")
		.subSettings(spawnerWithHeldItemFix, showSpawnerIcons, markTriggeredSpawners, new HeaderSetting(), hideMobPreview, hideParticles);

	@Override
	public void init() {
		super.init();
		WorldBlockOverlayRenderer.registerRenderObjectGenerator(this);
	}

	// Spawner with held item fix

	@EventListener
	private void onPacketSend(ItemUseEvent.Pre event) {
		if (!spawnerWithHeldItemFix.get() || event.stack == null || !ServerCheck.isOnGrieferGames() || player().isSneaking())
			return;

		if (event.stack != player().getHeldItem())
			// Packet probably was sent by a mod / addon
			return;

		Block clickedBlock = world().getBlockState(event.pos).getBlock();
		if (clickedBlock != Blocks.mob_spawner || MinecraftUtil.isInFarmwelt())
			return;

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

			resourceLocation = new ResourceLocation("griefer_utils/icons/mob_icons/outlined_minecraft/" + EntityList.getEntityString(entity).toLowerCase() + ".png");
		}

		@Override
		public void render(BlockPos pos, float partialTicks, int chunksFromPlayer) {
			if (markTriggeredSpawners.get() && (boolean) Reflection.invoke(mobSpawnerBaseLogic, "isActivated")) {
				GlStateManager.disableDepth();
				RenderUtil.drawBoxOutlines(new AxisAlignedBB(pos, pos.add(1, 1, 1)), new Color(0xFFFF00), 3);
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
				drawUtils().drawTexturedModalRect(0, 0, 256, 256);
				GlStateManager.popMatrix();
				GlStateManager.rotate(90, 0, 1, 0);
			}

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.26f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			drawUtils().drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, -1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.76f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			drawUtils().drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}

	}

}
