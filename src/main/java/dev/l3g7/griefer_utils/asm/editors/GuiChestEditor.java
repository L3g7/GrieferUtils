package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EventHandler;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.*;

@ClassTarget("net.minecraft.client.gui.inventory.GuiChest")
public class GuiChestEditor {

	/**
	 * ASM for DrawGuiContainerForegroundLayerEvent
	 */
	@MethodTarget(name = "drawGuiContainerForegroundLayer", parameters = {"int", "int"}, returnValue = "void")
	public static void editDrawGuiContainerForegroundLayer() {
		insertAtStart(
			varInsn(ALOAD, 0),
			methodInsn(INVOKESTATIC, EventHandler.drawGuiContainerForegroundLayer())
		);
	}

}
