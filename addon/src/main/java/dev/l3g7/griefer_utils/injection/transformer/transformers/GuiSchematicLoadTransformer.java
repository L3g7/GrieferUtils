package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.client.gui.load.GuiSchematicLoad")
public class GuiSchematicLoadTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("loadSchematic", "()V");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() != INVOKESTATIC)
				continue;

			MethodInsnNode min = (MethodInsnNode) node;
			if (!min.name.equals("moveSchematicToPlayer"))
				continue;

			iterator.set(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "setPositionAfterLoading", "(Lcom/github/lunatrius/schematica/client/world/SchematicWorld;)V", false));
			break;
		}
	}

}
