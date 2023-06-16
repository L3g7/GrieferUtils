package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

@Target("com.github.lunatrius.schematica.world.schematic.SchematicFormat")
public class SchematicFormatTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode methodNode = getMethod("readFromFile", "(Ljava/io/File;)Lcom/github/lunatrius/schematica/api/ISchematic;");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() != ASTORE)
				continue;

			iterator.next();
			iterator.add(new VarInsnNode(ALOAD, 1));
			iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "readFromNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V", false));
			break;
		}
	}

}
