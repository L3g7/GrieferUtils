package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.MappingNode;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.*;
import static dev.l3g7.griefer_utils.asm.util.ASMUtil.*;

@ClassTarget("net.minecraft.client.renderer.EntityRenderer")
public class EntityRendererEditor implements Opcodes {

    /**
     * ASM for RenderFogCheckEvents
     */
    @MethodTarget(name = "setupFog", parameters = {"int", "float"}, returnValue = "void")
    public static void editSetupFog() {
        addCheck(ICONST_0, Potion.blindness, EntityLivingBase.isPotionActive()); // BLINDNESS
        addCheck(ICONST_1, Block.getMaterial(), Mappings.Material.water); // WATER
        addCheck(ICONST_2, Block.getMaterial(), Mappings.Material.lava); // LAVA
    }

    private static void addCheck(int opcode, MappingNode... targets) {
        JumpInsnNode checkJumpInsn = (JumpInsnNode) findByMappings(targets).getFollowing();

        insertAfter(checkJumpInsn,
                insn(opcode), // Fog type
                methodInsn(Opcodes.INVOKESTATIC, EventHandler.shouldRenderFog()),
                jumpInsn(Opcodes.IFEQ, checkJumpInsn) // if not should render fog, skip to checkJumpInsn (the label the original if jumps to)
        );

    }

}
