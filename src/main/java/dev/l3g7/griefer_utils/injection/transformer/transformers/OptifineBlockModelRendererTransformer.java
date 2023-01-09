package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.features.world.furniture.optifine_patches.OptifineBlockModelRenderer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import dev.l3g7.griefer_utils.util.misc.Mapping;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static dev.l3g7.griefer_utils.util.misc.Mapping.MappingTarget.SRG;

/**
 * Fixes furniture not working correctly with Optifine
 */
@Target("net.minecraft.client.renderer.BlockModelRenderer")
public class OptifineBlockModelRendererTransformer extends Transformer {

	@Override
	protected void process() {
		try {
			Class.forName("optifine.OptiFineForgeTweaker");
		} catch (ClassNotFoundException e) {
			return;
		}

		for (String name : new String[] {"renderModelSmooth", "renderModelFlat"}) {
			MethodNode method = getMethod(name, "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Z");
			ListIterator<AbstractInsnNode> it = method.instructions.iterator();

			InsnList insns = new InsnList();
			insns.add(new VarInsnNode(ALOAD, 2));
			insns.add(new VarInsnNode(ALOAD, 3));
			insns.add(new VarInsnNode(ALOAD, 4));
			insns.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OptifineBlockModelRenderer.class), "getModel", Mapping.mapMethodDesc(SRG, "(Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;)Lnet/minecraft/client/resources/model/IBakedModel;"), false));
			insns.add(new VarInsnNode(ASTORE, 2));
			method.instructions.insertBefore(it.next(), insns);

			while (it.hasNext()) {
				AbstractInsnNode node = it.next();
				if (!matches(node, INVOKEINTERFACE, "net/minecraft/client/resources/model/IBakedModel", "getFaceQuads", "(Lnet/minecraft/util/EnumFacing;)Ljava/util/List;"))
					continue;

				insns = new InsnList();
				insns.add(new VarInsnNode(ALOAD, 2));
				insns.add(new VarInsnNode(ALOAD, 3));
				insns.add(new VarInsnNode(ALOAD, 4));
				insns.add(new VarInsnNode(ALOAD, 14));
				insns.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(OptifineBlockModelRenderer.class), "getQuads", Mapping.mapMethodDesc(SRG, "(Ljava/util/List;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Ljava/util/List;"), false));
				method.instructions.insert(node, insns);
			}
		}
	}

}
