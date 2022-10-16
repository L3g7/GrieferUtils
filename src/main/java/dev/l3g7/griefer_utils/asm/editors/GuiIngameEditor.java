package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.findByMappings;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.insertAfter;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.*;

@ClassTarget("net.minecraft.client.gui.GuiIngame")
public class GuiIngameEditor implements Opcodes {

	/**
	 * ASM to expand the maximum scoreboard size
	 */
	@MethodTarget(name = "renderScoreboard", parameters = {"net.minecraft.scoreboard.ScoreObjective", "net.minecraft.client.gui.ScaledResolution"}, returnValue = "void")
	public static void editRenderScoreboard() {
		AbstractInsnNode checkStart = findByMappings(Mappings.List.size()).getPrevious().getPrevious();

		insertAfter(checkStart,
			varInsn(ALOAD, 4),
			methodInsn(INVOKESTATIC, Mappings.ScoreBoardHandler.filterScores()),
			varInsn(ASTORE, 4),
			methodInsn(INVOKESTATIC, Mappings.ScoreBoardHandler.shouldNotUnlockScoreboard()),
			jumpInsn(Opcodes.IFEQ, "scoreboardSizeCheckSkip")
		);

		AbstractInsnNode checkEnd = findByMappings(Mappings.GuiIngame.getFontRenderer()).getPrevious().getPrevious();

		insertAfter(checkEnd.getPrevious().getPrevious(),
			label("scoreboardSizeCheckSkip")
		);
	}

}
