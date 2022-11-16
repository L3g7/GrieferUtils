package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.event.events.render.RenderBarrierCheckEvent;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Injects the {@link RenderBarrierCheckEvent} into {@link WorldClient#doVoidFogParticles(int, int, int)}.
 */
@Target("net.minecraft.client.multiplayer.WorldClient")
public class WorldClientTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode method = getMethod("doVoidFogParticles", "(III)V");
		ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			if (matches(node, INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", "getBlock", "()Lnet/minecraft/block/Block;")
				&& matches(node.getNext(), GETSTATIC, "net/minecraft/init/Blocks", "barrier", "Lnet/minecraft/block/Block;")) {
				AbstractInsnNode jumpNode =
					node           // iblockstate.getBlock()
					.getPrevious() // flag
					.getPrevious();// IFEQ L17

				LabelNode blockCheckLabel = new LabelNode();
				method.instructions.insert(jumpNode, blockCheckLabel);

				InsnList insns = new InsnList();
				insns.add(new JumpInsnNode(IFNE, blockCheckLabel));
				insns.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(WorldClientTransformer.class), "shouldRenderBarriers", "()Z", false));
				method.instructions.insertBefore(jumpNode, insns);
			}
		}
	}

	public static boolean shouldRenderBarriers() {
		return MinecraftForge.EVENT_BUS.post(new RenderBarrierCheckEvent());
	}

}
