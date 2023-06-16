package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.client.renderer.chunk.overlay.RenderOverlay")
public class RenderOverlayTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("func_178581_b", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() != INVOKESTATIC)
				continue;

			MethodInsnNode min = (MethodInsnNode) node;
			if (!min.name.equals("drawCuboid"))
				continue;

			iterator.set(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/HighlightSchematicaBlocks", "drawCuboid", min.desc, false));
			break;
		}
	}

}
