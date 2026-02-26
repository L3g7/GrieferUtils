/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class ApproximateEntityKillEvent extends Event {

	public static final Set<Entity> attackedEntities = new HashSet<>();

	public final Entity entity;

	public ApproximateEntityKillEvent(Entity entity) {
		this.entity = entity;
	}

	@EventListener
	private static void onPacketSend(PacketEvent.PacketSendEvent<C02PacketUseEntity> event) {
		if (event.packet.getAction() != C02PacketUseEntity.Action.ATTACK)
			return;

		attackedEntities.retainAll(world().getLoadedEntityList());
		attackedEntities.add(event.packet.getEntityFromWorld(world()));
	}

	@EventListener
	private static void onWorldUnload(WorldUnloadEvent event) {
		attackedEntities.clear();
	}

	@Mixin(EntityLivingBase.class)
	private static abstract class MixinEntityLivingBase {
		@Inject(method = "onDeath", at = @At("HEAD"))
		private void injectOnDeath(DamageSource source, CallbackInfo ci) {
			if (attackedEntities.remove(this))
				new ApproximateEntityKillEvent((Entity) (Object) this).fire();
		}
	}

}
