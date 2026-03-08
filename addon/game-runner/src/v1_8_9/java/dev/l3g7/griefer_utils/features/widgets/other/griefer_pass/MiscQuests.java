package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.*;
import dev.l3g7.griefer_utils.core.events.BlockEvent.BlockBrokeEvent;
import dev.l3g7.griefer_utils.core.events.BlockEvent.BlockInteractEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageAboutToBeSentEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceivedEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.LuckyBlockType;
import dev.l3g7.griefer_utils.features.uncategorized.debug.PacketDumper;
import dev.l3g7.griefer_utils.features.world.bsf.BSF;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

abstract class MiscQuests {

	static class WalkQuest extends AbstractQuest {

		private double amount = 0;
		private static Vec3 previousPosition = null;

		@EventListener
		private void onTick(ClientTickEvent event) {
			EntityPlayer player = player();
			if (player == null)
				return;

			Vec3 pos = player.getPositionVector();
			if (previousPosition == null) {
				previousPosition = pos;
				return;
			}

			double distance = previousPosition.distanceTo(pos);
			if (distance == 0)
				return;

			double previousAmount = this.amount;
			this.amount += amount;
			super.increaseAmount(((int) this.amount) - ((int) previousAmount));
			previousPosition = pos;
		}

		@EventListener
		private void onTp(PacketReceiveEvent<S08PacketPlayerPosLook> event) {
			previousPosition = new Vec3(event.packet.getX(), event.packet.getY(), event.packet.getZ());
		}

	}

	static class EatQuest extends AbstractQuest {

		private ItemStack target;

		@Override
		public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
			super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
			this.target = Translator.getItem(matcher.group(2));
		}

		@EventListener
		private void onUseItemFinish(ItemUseEvent.Finish event) {
			if (target.isItemEqual(event.itemStack))
				increaseAmount();
		}

	}

	static class BreakQuest extends AbstractQuest {

		private Pair<Block, Integer> target;

		@Override
		public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
			super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
			this.target = Translator.getBlock(matcher.group(2));
		}

		@EventListener
		private void onBlockBreak(BlockBrokeEvent event) {
			Block block = event.state.getBlock();
			if (block == target.a && block.getMetaFromState(event.state) == target.b)
				increaseAmount();
		}

	}

	static class RideBoatOrMinecartQuest extends AbstractQuest {
		@EventListener
		private void onAttach(PacketReceiveEvent<S1BPacketEntityAttach> event) {
			if (event.packet.getVehicleEntityId() == -1 || world().getEntityByID(event.packet.getEntityId()) != player())
				return;

			Entity vehicle = world().getEntityByID(event.packet.getVehicleEntityId());
			if (vehicle instanceof EntityBoat || vehicle instanceof EntityMinecartEmpty)
				increaseAmount();
		}

	}

	static class JoinCitybuildQuest extends AbstractQuest {
		@EventListener
		private void onCitybuildJoin(CitybuildJoinEvent.Early event) {
			if (event.citybuild != Citybuild.ANY && event.citybuild != Citybuild.MAGIC_FOREST)
				increaseAmount();
		}
	}

	static class KillQuest extends AbstractQuest {

		private Class<? extends Entity> target;
		private String entityString;
		private boolean requireNether = false;

		@Override
		public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
			super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
			String target = matcher.group(2);
			if (target.endsWith(" im Nether")) {
				target = target.substring(0, target.length() - " im Nether".length());
				requireNether = true;
			}

			if ((entityString = target).equals("Witherskelett"))
				this.target = EntitySkeleton.class;
			else
				this.target = Translator.getEntity(target);
		}

		@EventListener
		private void onEntityKill(ApproximateEntityKillEvent event) {
			if (target.isInstance(event.entity) && (!requireNether || player().dimension == -1)) {
				if (!entityString.equals("witherskelett") || ((EntitySkeleton) event.entity).getSkeletonType() == 1)
					increaseAmount();
			}
		}
	}

	static class TameQuest extends AbstractQuest {

		private final Set<Entity> interactedAnimals = new HashSet<>();

		@EventListener
		private void onPacketSend(PacketEvent.PacketSendEvent<C02PacketUseEntity> event) {
			if (event.packet.getAction() != C02PacketUseEntity.Action.INTERACT)
				return;

			Entity entity = event.packet.getEntityFromWorld(world());
			if (!(entity instanceof EntityTameable et) || et.isTamed())
				return;

			interactedAnimals.retainAll(world().getLoadedEntityList());
			interactedAnimals.add(entity);
		}

		@EventListener
		private void onWorldUnload(WorldUnloadEvent event) {
			interactedAnimals.clear();
		}

		@EventListener
		private void onStatus(PacketReceivedEvent<S19PacketEntityStatus> event) {
			if (event.packet.getOpCode() == 6 && event.packet.getEntity(world()) instanceof EntityTameable et)
				interactedAnimals.remove(et);
		}

		@EventListener
		private void onMetaupdate(PacketReceivedEvent<S1CPacketEntityMetadata> event) {
			Entity entity = world().getEntityByID(event.packet.getEntityId());
			if (!(entity instanceof EntityTameable et))
				return;

			if (!et.isTamed() || et.getOwner() != player())
				return;

			if (!interactedAnimals.contains(entity))
				return;

			increaseAmount();
			interactedAnimals.remove(et);
		}
	}

	static class ReceiveEffectsQuest extends AbstractQuest {
		@EventListener
		private void onEffect(PacketReceivedEvent<S1DPacketEntityEffect> event) {
			if (event.packet.getEntityId() == player().getEntityId())
				increaseAmount();
		}
	}

	static class PlaceQuest extends AbstractQuest {

		private BlockPos waitingForUpdate;
		private boolean isFlintAndSteel;

		@EventListener
		private void onPlace(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
			Citybuild cb = MinecraftUtil.getCurrentCitybuild();
			if (BSF.isInFarmwelt() || cb == Citybuild.ANY || cb == Citybuild.MAGIC_FOREST || cb == Citybuild.LAVA || cb == Citybuild.WATER)
				return;

			if (event.packet.getPlacedBlockDirection() == 255 || event.packet.getStack() == null)
				return;

			waitingForUpdate = event.packet.getPosition().offset(EnumFacing.getFront(event.packet.getPlacedBlockDirection()));
			isFlintAndSteel = event.packet.getStack().getItem() == Items.flint_and_steel;
		}

		@EventListener
		private void onBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
			if (event.packet.getBlockPosition().equals(waitingForUpdate)) {
				waitingForUpdate = null;
				Block block = event.packet.getBlockState().getBlock();
				if (block != Blocks.air) {
					increaseAmount();
					if (isFlintAndSteel)
						increaseAmount(); // Flint and Steel increases the amount by 2 (for some reason)
				}

				isFlintAndSteel = false;
			}
		}
	}

	static class FishQuest extends AbstractQuest {

		private ItemStack target;

		@Override
		public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
			super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
			this.target = Translator.getItem(matcher.group(2));
		}

		private boolean waitingForUpdate = false;
		private long readTime = -1;

		@EventListener
		private void onPlace(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
			if (event.packet.getPlacedBlockDirection() != 255 || player().fishEntity == null)
				return;

			readTime = -1;
			waitingForUpdate = true;
		}

		@EventListener
		private void onSetSlot(PacketReceiveEvent<S2FPacketSetSlot> event) {
			if (!waitingForUpdate)
				return;

			ItemStack targetStack = player().openContainer.getSlot(event.packet.func_149173_d()).getStack();
			if (targetStack == null || targetStack != player().getHeldItem())
				return;

			readTime = PacketDumper.getLastReadTime();
			waitingForUpdate = false;
		}

		@EventListener
		private void onEntityMetaData(PacketReceiveEvent<S1CPacketEntityMetadata> event) {
			if (readTime == -1 || !(world().getEntityByID(event.packet.getEntityId()) instanceof EntityItem))
				return;

			for (DataWatcher.WatchableObject wo : event.packet.func_149376_c())
				if (wo.getDataValueId() == 10 && wo.getObject() instanceof ItemStack is && target.isItemEqual(is))
					increaseAmount();

			readTime = -1;
		}
	}

	static class CommandSendQuest extends AbstractQuest {

		private String target;

		@Override
		public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
			super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
			this.target = "/" + matcher.group(2).toLowerCase();
		}

		@EventListener(priority = Priority.LOWEST)
		private void onMessageSend(MessageAboutToBeSentEvent event) {
			if (event.message.startsWith(target))
				increaseAmount();
		}

	}

	static class ShootBowQuest extends AbstractQuest {

		private long startPulling = 0;
		private int pendingShoots = 0;

		@EventListener
		private void onBowPullStart(ItemUseEvent.Post event) {
			if (event.stackBeforeUse != null && event.stackBeforeUse.getItem() == Items.bow)
				startPulling = System.currentTimeMillis();
		}

		@EventListener
		private void onBowRelease(PacketSendEvent<C07PacketPlayerDigging> event) {
			if (event.packet.getStatus() != C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
				return;

			if (player().getHeldItem() == null || player().getHeldItem().getItem() != Items.bow)
				return;

			if (System.currentTimeMillis() - startPulling <= 100)
				return; // Didn't charge long enough

			pendingShoots++;
		}

		@EventListener
		private void onWorldUnload(WorldUnloadEvent event) {
			pendingShoots = 0;
			startPulling = 0;
		}

		@EventListener
		private void onSound(PacketReceiveEvent<S29PacketSoundEffect> event) {
			if (pendingShoots <= 0)
				return;

			S29PacketSoundEffect p = event.packet;
			if (player().getDistance(p.getX(), p.getY(), p.getZ()) >= 0.25)
				return;

			pendingShoots--;
			increaseAmount();
		}

	}

	static class PickupItemsQuest extends AbstractQuest {

		@EventListener
		private void onItemPickUp(PacketReceiveEvent<S0DPacketCollectItem> event) {
			if (event.packet.getEntityID() != player().getEntityId())
				return;

			if (!(world().getEntityByID(event.packet.getCollectedItemEntityID()) instanceof EntityItem item) || item.getEntityItem() == null)
				return;

			increaseAmount(item.getEntityItem().stackSize);
		}

	}

	static class OpenLuckyBlockQuest extends AbstractQuest {

		private final List<Long> luckyBlockPlaceTimes = new ArrayList<>();

		@EventListener
		private void onPlace(BlockInteractEvent event) {
			ItemStack heldItem = heldItem();
			if (heldItem == null
				|| !heldItem.hasTagCompound()
				|| heldItem.getTagCompound().getInteger("HideFlags") != 19
				|| EnchantmentHelper.getEnchantments(heldItem).get(51) != -1)
				return;

			luckyBlockPlaceTimes.add(System.currentTimeMillis());
		}

		@EventListener(priority = Priority.HIGH)
		private void onLuckyBlockMessage(MessageReceiveEvent event) {
			if (!event.message.getUnformattedText().startsWith("[LuckyBlock]"))
				return;

			luckyBlockPlaceTimes.removeIf(luckyBlockPlaceTime -> {
				long passedTime = System.currentTimeMillis() - luckyBlockPlaceTime;
				return passedTime > 15_000;
			});

			if (luckyBlockPlaceTimes.isEmpty())
				return;

			if ((System.currentTimeMillis() - luckyBlockPlaceTimes.get(0)) >= 10_000) {
				luckyBlockPlaceTimes.remove(0);
				increaseAmount();
			}
		}

	}

}
