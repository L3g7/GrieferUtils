package dev.l3g7.griefer_utils.asm.editors;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.*;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.methodInsn;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.varInsn;

@ClassTarget("net.labymod.ingamechat.renderer.ChatRenderer")
public class ChatRendererEditor implements Opcodes {

	/**
	 * ASM to render the skulls in front of messages
	 */
	@MethodTarget(name = "renderChat", parameters = {"int"}, returnValue = "void")
	public static void editRenderChat() {
		AbstractInsnNode textRender = findByMappings(Mappings.GlStateManager.enableBlend()).getFollowing();

		insertAfter(textRender,
			varInsn(ALOAD, 23),
			varInsn(ILOAD, 29),
			methodInsn(INVOKESTATIC, Mappings.MessageSkulls.renderSkull())
		);
	}

	/**
	 * ASM for AddChatLineEvents
	 */
	@MethodTarget(name = "addChatLine", parameters = {"java.lang.String", "boolean", "java.lang.String", "java.lang.Object", "int", "int", "java.lang.Integer", "boolean"}, returnValue = "void")
	public static void editAddChatLine() {
		insertAtStart(
			varInsn(ALOAD, 1),
			methodInsn(INVOKESTATIC, Mappings.EventHandler.addChatLine())
		);
	}

}
