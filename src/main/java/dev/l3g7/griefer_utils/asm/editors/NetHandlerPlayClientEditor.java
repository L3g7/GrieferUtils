package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;

import static dev.l3g7.griefer_utils.asm.mappings.Mappings.EventHandler;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.*;

@ClassTarget("net.minecraft.client.network.NetHandlerPlayClient")
public class NetHandlerPlayClientEditor {

	/**
	 * ASM for PacketSendEvent
	 */
	@MethodTarget(name = "addToSendQueue", parameters = {"net.minecraft.network.Packet"}, returnValue = "void")
	public static void editAddToSendQueue() {
		insertAtStart(
				varInsn(ALOAD, 1),
				methodInsn(INVOKESTATIC, EventHandler.shouldSendPacket()),
				jumpInsn(IFEQ, "addToSendQueueEnd")
		);

		insertBeforeReturns(
				label("addToSendQueueEnd")
		);
	}

}
