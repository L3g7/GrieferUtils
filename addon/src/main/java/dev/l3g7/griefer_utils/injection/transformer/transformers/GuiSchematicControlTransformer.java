package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.client.gui.control.GuiSchematicControl")
public class GuiSchematicControlTransformer extends Transformer {


	@Override
	protected void process() {
		MethodNode methodNode = getMethod("func_73866_w_", "()V");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() != INVOKEINTERFACE)
				continue;

			MethodInsnNode min = (MethodInsnNode) node;
			if (!min.name.equals("clear"))
				continue;

			iterator.next();
			iterator.add(new VarInsnNode(ALOAD, 0));
			iterator.add(new FieldInsnNode(GETFIELD, "com/github/lunatrius/schematica/client/gui/control/GuiSchematicControl", "field_146292_n", "Ljava/util/List;"));
			iterator.add(new VarInsnNode(ALOAD, 0));
			iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "addGuiButton", "(Ljava/util/List;Lnet/minecraft/client/gui/GuiScreen;)V", false));
			break;
		}

		methodNode = getMethod("func_146284_a", "(Lnet/minecraft/client/gui/GuiButton;)V");
		iterator = methodNode.instructions.iterator();
		iterator.add(new VarInsnNode(ALOAD, 1));
		iterator.add(new VarInsnNode(ALOAD, 0));
		iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "onActionPerformed", "(Lnet/minecraft/client/gui/GuiButton;Lnet/minecraft/client/gui/GuiScreen;)V", false));
	}

}
