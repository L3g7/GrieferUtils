package dev.l3g7.griefer_utils.asm.editors;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import dev.l3g7.griefer_utils.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.findByMappings;
import static dev.l3g7.griefer_utils.asm.util.InsnListUtil.insertAfter;
import static dev.l3g7.griefer_utils.asm.util.InsnUtil.*;

@ClassTarget("net.labymod.ingamechat.GuiChatCustom")
public class GuiChatCustomEditor implements Opcodes {

	/**
	 * ASM to fix right-click-on-mobile-player crash
	 */
	@MethodTarget(name = "drawScreen", parameters = {"int", "int", "float"}, returnValue = "void")
	public static void editDrawScreen() {

		AbstractInsnNode start = findByMappings(Mappings.NameHistoryUtil.getNameHistory()).getPrevious().getPrevious();

		// Return if isMobilePlayer() returns true
		insertAfter(start,
				varInsn(ALOAD, 5),
				varInsn(ILOAD, 1),
				varInsn(ILOAD, 2),
				methodInsn(INVOKESTATIC, Mappings.GuiChatCustomEditor.isMobilePlayer()),
				jumpInsn(IFEQ, "notMobile"),
				insn(RETURN),
				label("notMobile")
		);
	}


	public static boolean isMobilePlayer(String name, int mouseX, int mouseY) {
		if (name.startsWith("!")) {
			Reflection.invoke(Minecraft.getMinecraft().currentScreen, "drawHoveringText", ImmutableList.of("§cFür Handyspieler können keine Namensänderungen anzeigt werden."), mouseX, mouseY);
			GlStateManager.disableLighting();
			return true;
		}

		return false;
	}
}