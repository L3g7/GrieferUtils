package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EnumParticleTypes;
import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EventHandler;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.findByMappings;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.insertAfter;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

@ClassTarget("net.minecraft.client.multiplayer.WorldClient")
public class ClientWorldEditor {
	/**
	 * ASM for ShowBarriers
	 */
	@MethodTarget(name = "doVoidFogParticles", parameters = {"int", "int", "int"}, returnValue = "void")
	public static void editVoidFog() {
		// Find the start and the end of the flag check
		AbstractInsnNode flagCheckEnd = findByMappings(EnumParticleTypes.barrier).getPrevious();
		for (int i = 0; i < 7; i++)
			flagCheckEnd = flagCheckEnd.getPrevious();

		// Add a new label
		insertAfter(flagCheckEnd,
				label("flagCheckEnd")
		);

		AbstractInsnNode flagCheckStart = flagCheckEnd.getPrevious().getPrevious();

		// Jump to the new label if ShowBarriers is active
		insertAfter(flagCheckStart,
				methodInsn(INVOKESTATIC, EventHandler.shouldRenderBarrier()),
				jumpInsn(Opcodes.IFEQ, "flagCheckEnd")
		);
	}
}
