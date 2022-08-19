package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EventHandler;
import static dev.l3g7.griefer_utils.asm.util.ASMUtil.*;

@ClassTarget("net.minecraft.entity.player.EntityPlayer")
public class EntityPlayerEditor {

	/**
	 * ASM for RenderInvisibilityCheckEvent
	 */
	@MethodTarget(name = "isInvisibleToPlayer", parameters = {"net.minecraft.entity.player.EntityPlayer"}, returnValue = "boolean")
	public static void editIsInvisibleToPlayer() {
		EntityEditor.editIsInvisibleToPlayer();
	}

	/**
	 * ASM for DisplayNameRenderEvents
	 */
	@MethodTarget(name = "getDisplayName", returnValue = "net.minecraft.util.IChatComponent")
	public static void editGetDisplayName() {
		// Call modifyDisplayName before returning
		insertBeforeReturns(
				varInsn(ALOAD, 0), // push itself ('this') on stack (along with the IChatComponent)
				methodInsn(INVOKESTATIC, EventHandler.modifyDisplayName())
		);
	}

}
