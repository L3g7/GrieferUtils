package dev.l3g7.griefer_utils.features.features;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class NpcEntityGhostHand extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("NPC-Klick-Helfer")
			.description("Ermöglicht das Klicken auf NPCs durch Entities.")
			.icon("cursor")
			.config("features.npc_ghost_hand.active")
			.defaultValue(true);

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public NpcEntityGhostHand() {
		super(Category.FEATURE);
	}

	@SubscribeEvent
	public void onClick(MouseEvent event) {
		if (!isActive() || !isOnGrieferGames() || world() == null || player() == null)
			return;

		// Only intercept right clicks
		if (event.isCanceled() || !event.buttonstate || event.button != 1)
			return;

		// Don't intercept if targeted entity is a NPC
		if (isNPC(mc().pointedEntity))
			return;

		float reachDistance = mc().playerController.getBlockReachDistance();

		Timer timer = Reflection.get(mc(), "timer", "field_71428_T", "Y");
		float partialTicks = timer.renderPartialTicks;
		Vec3 eyes = player().getPositionEyes(partialTicks);

		// Check if block is in the way (Don't want to create a cheat xD)
		MovingObjectPosition targetedBlock = mc().getRenderViewEntity().rayTrace(reachDistance, partialTicks);
		if (targetedBlock != null && targetedBlock.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
			return;

		for (Entity entity : world().loadedEntityList) {
			if (!(entity instanceof EntityOtherPlayerMP))
				continue;

			// Skip if entity isn't a NPC
			if (!isNPC(entity))
				continue;

			// Check if entity is hit
			Vec3 lookVec = player().getLook(partialTicks);
			Vec3 maxTracePos = eyes.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

			float collisionSize = entity.getCollisionBorderSize();
			AxisAlignedBB hitBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);

			MovingObjectPosition result = hitBox.calculateIntercept(eyes, maxTracePos);

			if (result != null) {
				mc().playerController.interactWithEntitySendPacket(player(), entity);
				return;
			}
		}
	}

	private boolean isNPC(Entity entity) {
		if (!(entity instanceof EntityOtherPlayerMP))
			return false;

		EntityOtherPlayerMP npc = ((EntityOtherPlayerMP) entity);
		GameProfile gp = npc.getGameProfile();
		return gp.getName().startsWith("§") && !NameCache.hasUUID(gp.getId());
	}
}