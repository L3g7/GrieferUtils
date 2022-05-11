package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import org.objectweb.asm.tree.AbstractInsnNode;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.GlStateManager;
import static dev.l3g7.griefer_utils.asm.mappings.Mappings.TrueSight;
import static dev.l3g7.griefer_utils.asm.util.ASMUtil.*;

@ClassTarget("net.minecraft.client.renderer.entity.RendererLivingEntity")
public class RendererLivingEntityEditor {

    /**
     * ASM for changeable TrueSight opacity
     */
    @MethodTarget(name = "renderModel", parameters = {"net.minecraft.entity.EntityLivingBase", "float", "float", "float", "float", "float", "float"}, returnValue = "void")
    public static void editRenderModel() {
        // Find alpha value node (node before GlStateManager.color call)
        AbstractInsnNode alphaValueNode = findByMappings(GlStateManager.color()).getPrevious();

        // Replace value with getRenderModelAlpha call
        replace(alphaValueNode, methodInsn(INVOKESTATIC, TrueSight.getRenderModelAlpha()));
    }

}
