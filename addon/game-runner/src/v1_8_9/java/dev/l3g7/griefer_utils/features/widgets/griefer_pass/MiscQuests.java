package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.BlockEvent.BlockBrokeEvent;
import dev.l3g7.griefer_utils.core.events.ItemUseEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

abstract class MiscQuests {

	static class WalkQuest extends AbstractQuest {

		private double amount = 0;
		private static Vec3 previousPosition = null;

		protected WalkQuest(Matcher matcher, int maxAmount) {
			super(matcher, maxAmount);
		}

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
		private void onTp(PacketEvent.PacketReceiveEvent<S08PacketPlayerPosLook> event) {
			previousPosition = new Vec3(event.packet.getX(), event.packet.getY(), event.packet.getZ());
		}

	}

	static class EatQuest extends AbstractQuest {

		private final Item item;

		protected EatQuest(Matcher matcher, int maxAmount) {
			super(matcher, maxAmount);
			this.item = Translator.getItem(matcher.group(2));
		}

		@EventListener
		private void onUseItemFinish(ItemUseEvent.Finish event) {
			if (event.itemStack.getItem() == item)
				increaseAmount();
		}

	}

	static class BreakQuest extends AbstractQuest {

		private final Pair<Block, Integer> blockPair;

		protected BreakQuest(Matcher matcher, int maxAmount) {
			super(matcher, maxAmount);
			this.blockPair = Translator.getBlock(matcher.group(2));
		}

		@EventListener
		private void onBlockBreak(BlockBrokeEvent event) {
			Block block = event.state.getBlock();
			if (block == blockPair.a && block.getMetaFromState(event.state) == blockPair.b)
				increaseAmount();
		}

	}

	static class PortalQuest extends AbstractQuest {
		protected PortalQuest(Matcher matcher, int maxAmount) {
			super(matcher, maxAmount);
			if (maxAmount != 1)
				BugReporter.reportError(new Throwable("PortalQuest with maxAmount " + maxAmount));
		}
	}

}
