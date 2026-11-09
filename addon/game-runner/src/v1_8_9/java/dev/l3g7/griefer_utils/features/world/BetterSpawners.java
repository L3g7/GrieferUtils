/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.ItemUseEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TileEntityDataSetEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.GeometryRenderUtil;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.RenderChunkManager;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.WorldRenderChunkManager;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.SpecificItemSaver;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

@Singleton
public class BetterSpawners extends Feature implements RenderObject.Generator {

	private BlockPos lastClickedSpawner = null;
	private BlockPos lastOpenedSpawner = null;

	private static final List<BlockPos> spawners = new ArrayList<>();

	private int itemMoveOrigin = -1;

	private final SwitchSetting spawnerWithHeldItemFix = SwitchSetting.create()
		.name("Spawner mit Item öffnen")
		.description("Ermöglicht das Öffnen von Spawnern auf öffentlichen Grundstücken, auch wenn man ein Item / einen Block in der Hand hält.")
		.icon("spawner");

	public final SwitchSetting hideMobPreview = SwitchSetting.create()
		.name("Entity verstecken")
		.description("Ob die kleine, sich drehende Vorschau des zu spawnenden Entities versteckt werden soll.")
		.icon("invisibility");

	public final SwitchSetting hideParticles = SwitchSetting.create()
		.name("Partikel verstecken")
		.description("Ob die Partikel vom Spawner versteckt werden sollen.")
		.icon("particle");

	private final SwitchSetting showSpawnerIcons = SwitchSetting.create()
		.name("Spawner-Icons anzeigen")
		.description("Zeigt an Spawnern ein Icon des Mobs an, das gespawnt wird.")
		.icon("mob_icons/outlined_minecraft/silverfish")
		.callback(RenderChunkManager::reloadAll);

	private final SwitchSetting markTriggeredSpawners = SwitchSetting.create()
		.name("Aktivierte Spawner markieren")
		.description("Markiert Spawner, die von der Position des Spielers aktiviert werden.")
		.icon("light_bulb");

	private final SwitchSetting markLastOpenedSpawner = SwitchSetting.create()
		.name("Zuletzt geöffneten Spawner markieren")
		.description("Markiert den Spawner, der als letztes geöffnet wurde.")
		.icon("spawner")
		.callback(RenderChunkManager::reloadAll);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spawner verbessern")
		.description("Verbessert Spawner.")
		.icon("spawner")
		.subSettings(spawnerWithHeldItemFix, showSpawnerIcons, markTriggeredSpawners, markLastOpenedSpawner, HeaderSetting.create(), hideMobPreview, hideParticles);

	public static BetterSpawners get() {
		return get(BetterSpawners.class);
	}

	// Spawner with held item fix

	@EventListener
	private void onItemUsePre(ItemUseEvent.Pre event) {
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

		if (SpecificItemSaver.getProtectionFor(event.stack).deniesItemPickup()) {
			labyBridge.notify("§cItem-Saver", "§cDas Item in deiner Hand ist im Item-Saver!");
			return;
		}

		event.cancel();
		for (int i = 0; i < player().inventory.mainInventory.length; i++) {
			if (player().inventory.getStackInSlot(i) == null) {
				itemMoveOrigin = i;
				break;
			}
		}

		if (itemMoveOrigin == -1) {
			labyBridge.notify("§eSpawner verbessern", "§eDein Inventar ist voll!");
			return;
		}

		move(false);
		mc().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(event.pos, event.side.getIndex(), null, event.hitX, event.hitY, event.hitZ));
	}

	private void move(boolean toHotbar) {
		if (itemMoveOrigin < 9)
			itemMoveOrigin += 36;

		int hotbarSlot = player().inventory.currentItem;
		short transactionID = player().openContainer.getNextTransactionID(player().inventory);
		mc().getNetHandler().addToSendQueue(new C0EPacketClickWindow(0, itemMoveOrigin, hotbarSlot, 2, null, transactionID));

		if (itemMoveOrigin >= 36)
			itemMoveOrigin -= 36;

		if (!toHotbar) {
			ItemStack stack = player().inventory.getStackInSlot(hotbarSlot);
			player().inventory.setInventorySlotContents(itemMoveOrigin, stack);
			player().inventory.setInventorySlotContents(hotbarSlot, null);
			return;
		}

		ItemStack originStack = player().inventory.getStackInSlot(itemMoveOrigin);
		ItemStack slotStack = player().inventory.getStackInSlot(hotbarSlot);
		player().inventory.setInventorySlotContents(hotbarSlot, originStack);
		player().inventory.setInventorySlotContents(itemMoveOrigin, slotStack);
		itemMoveOrigin = -1;
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (itemMoveOrigin != -1 && event.message.getFormattedText().startsWith("§r§8[§r§6GrieferGames§r§8] §r§cDer Spawner ist aktuell von §r§e"))
			move(true);
	}

	@EventListener
	private void onGuiClose(PacketSendEvent<C0DPacketCloseWindow> e) {
		TickScheduler.sync(() -> {
			if (itemMoveOrigin != -1)
				move(true);
		});
	}

	// Show spawner icons & mark last opened spawner

	@EventListener
	private void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Spawner - Lager") || lastClickedSpawner == null)
			return;

		if (Objects.equals(lastOpenedSpawner, lastClickedSpawner)) {
			lastClickedSpawner = null;
			return;
		}

		TileEntity tileEntity = world().getTileEntity(lastClickedSpawner);
		if (!(tileEntity instanceof TileEntityMobSpawner spawner))
			return;

		if (lastOpenedSpawner != null)
			WorldRenderChunkManager.get().setRenderObject(lastOpenedSpawner, null);

		WorldRenderChunkManager.get().setRenderObject(lastClickedSpawner, SpawnerRenderObject.of(spawner, true));
		lastOpenedSpawner = lastClickedSpawner;
		lastClickedSpawner = null;
	}

	@EventListener(triggerWhenDisabled = true)
	private void onServerSwitch(ServerSwitchEvent event) {
		lastOpenedSpawner = lastClickedSpawner = null;
	}

	@Override
	public RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world) {
		if (state.getBlock() != Blocks.mob_spawner)
			return null;

		TileEntity tileEntity = world.getTileEntity(worldPos);
		if (!(tileEntity instanceof TileEntityMobSpawner spawner))
			return null;

		spawners.add(worldPos);
		return SpawnerRenderObject.of(spawner, worldPos.equals(lastOpenedSpawner));
	}

	@EventListener(triggerWhenDisabled = true)
	private void onTileEntityDataSet(TileEntityDataSetEvent event) {
		if (!(event.tileEntity instanceof TileEntityMobSpawner spawner))
			return;

		BlockPos pos = event.tileEntity.getPos();
		WorldRenderChunkManager.get().setRenderObject(pos, SpawnerRenderObject.of(spawner, pos.equals(lastOpenedSpawner)));
	}

	// Show triggered spawners

	@EventListener
	private void renderTriggeredSpawners(RenderWorldLastEvent event) {
		if (!markTriggeredSpawners.get())
			return;

		spawners.removeIf(pos -> {
			if (!(world().getTileEntity(pos) instanceof TileEntityMobSpawner spawner))
				return true;

			boolean isActivated = Reflection.invoke(spawner.getSpawnerBaseLogic(), "isActivated");
			if (isActivated) {
				GlStateManager.disableDepth();
				GeometryRenderUtil.drawBoxOutlines(new AxisAlignedBB(pos, pos.add(1, 1, 1)), new Color(0xFFFF00), 3);
				GlStateManager.enableDepth();
			}

			return false;
		});
	}

	// Hide mobs

	@Mixin(TileEntityMobSpawnerRenderer.class)
	private static class MixinTileEntityMobSpawnerRenderer {

		@Unique
		private static final BetterSpawners BETTER_SPAWNERS = BetterSpawners.get();

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
		private static final BetterSpawners BETTER_SPAWNERS = BetterSpawners.get();

		@Redirect(method = "updateSpawner", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
		private void redirectSpawnParticle(World instance, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] p_175688_14_) {
			if (!BETTER_SPAWNERS.isEnabled() || !BETTER_SPAWNERS.hideParticles.get())
				instance.spawnParticle(particleType, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
		}

	}

	private record SpawnerRenderObject(TextureCutout texture, boolean isLastOpenedSpawner) implements RenderObject {

		private static final Quad BASE_QUAD = Quad.onYPlane(0.5, 0.5).translate(0, 0.51, 0);

		private static final Quad[] ICON_QUADS = new Quad[] {
			BASE_QUAD,
			BASE_QUAD.rotateX(180),
			BASE_QUAD.rotateX(270),
			BASE_QUAD.rotateX(270).rotateY(90),
			BASE_QUAD.rotateX(270).rotateY(180),
			BASE_QUAD.rotateX(270).rotateY(270)
		};

		public static SpawnerRenderObject of(TileEntityMobSpawner spawner, boolean isLastOpenedSpawner) {
			Entity entity = spawner.getSpawnerBaseLogic().func_180612_a(world());

			ResourceLocation resourceLocation = entity == null
				? new ResourceLocation("missing_entity_texture_null")
				: new ResourceLocation("griefer_utils", "icons/mob_icons/outlined_minecraft/" + EntityList.getEntityString(entity).toLowerCase() + ".png");

			return new SpawnerRenderObject(
				BetterSpawners.get().showSpawnerIcons.get() ? new TextureCutout(resourceLocation) : null,
				BetterSpawners.get().markLastOpenedSpawner.get() && isLastOpenedSpawner
			);
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof SpawnerRenderObject(TextureCutout otherTexture, boolean otherIsLastOpenedSpawner)
				&& Objects.equals(texture, otherTexture) && isLastOpenedSpawner == otherIsLastOpenedSpawner;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			if (texture != null)
				compiler.draw(texture, ICON_QUADS);

			if (!isLastOpenedSpawner)
				return;

			compiler.disable(GL_DEPTH_TEST, GL_TEXTURE_2D);
			compiler.setColor(new Color(0x6000FF00, true));
			GeometryRenderUtil.drawFilledBoxWhenRenderingStarted(compiler, new AxisAlignedBB(chunkPosition, chunkPosition.add(1, 1, 1)), false);
			compiler.enable(GL_DEPTH_TEST, GL_TEXTURE_2D);
		}

	}

}
