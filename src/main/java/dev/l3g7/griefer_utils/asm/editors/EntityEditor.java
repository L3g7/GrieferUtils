package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EventHandler;
import static dev.l3g7.griefer_utils.asm.util.ASMUtil.*;

@ClassTarget("net.minecraft.entity.Entity")
public class EntityEditor {

    /**
     * ASM for RenderBurningCheckEvent
     */
    @MethodTarget(name = "isBurning", returnValue = "boolean")
    public static void editIsBurning() {
        insertAtStart(
                varInsn(ALOAD, 0), // push itself ('this') on stack
                methodInsn(INVOKESTATIC, EventHandler.shouldNotBeBurning()),

                // If entity should be burning, continue normally
                jumpInsn(IFEQ, INJECT_END),

                // If reached this point, should not be burning -> return false
                insn(ICONST_0),
                insn(IRETURN)
        );
    }

    /**
     * ASM for RenderInvisibilityCheckEvent
     */
    @MethodTarget(name = "isInvisibleToPlayer", parameters = {"net.minecraft.entity.player.EntityPlayer"}, returnValue = "boolean")
    public static void editIsInvisibleToPlayer() {
        insertAtStart(
                varInsn(ALOAD, 0), // push itself ('this') on stack
                methodInsn(INVOKESTATIC, EventHandler.shouldBeVisible()),

                // If not should be visible, continue normally
                jumpInsn(IFEQ, INJECT_END),

                // If reached this point, should not be visible -> return false
                insn(ICONST_0),
                insn(IRETURN)
        );
    }

}
