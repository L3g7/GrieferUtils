package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.findByMappings;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.insertAfter;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.*;

@ClassTarget("net.labymod.ingamegui.modules.ScoreboardModule")
public class ScoreboardModuleEditor implements Opcodes {

	/**
	 * ASM to expand the maximum scoreboard size
	 */
	@MethodTarget(name = "renderScoreboard", parameters = {"net.minecraft.scoreboard.ScoreObjective", "double", "double", "boolean"}, returnValue = "void")
	public static void editRenderScoreboard() {
		AbstractInsnNode sizeCheck = findByMappings(Mappings.List.size()).getPrevious();
		AbstractInsnNode checkStart = sizeCheck.getPrevious().getPrevious();

		insertAfter(checkStart,
			varInsn(ALOAD, 10),
			methodInsn(INVOKESTATIC, Mappings.ScoreBoardHandler.filterScores()),
			varInsn(ASTORE, 10)
		);

		JumpInsnNode jumpNode = ((JumpInsnNode) sizeCheck.getNext().getNext().getNext());

		insertAfter(jumpNode,
			methodInsn(INVOKESTATIC, Mappings.ScoreBoardHandler.shouldNotUnlockScoreboard()),
			jumpInsn(Opcodes.IFEQ, jumpNode)
		);

	}

}
