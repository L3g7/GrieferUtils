package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.chat.chat_filter_templates.ChatFilterTemplates;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import net.labymod.core.asm.LabyModCoreMod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@Target("net.labymod.ingamechat.tabs.GuiChatFilter")
public class GuiChatFilterTransformer extends Transformer {

	@Override
	protected void process() {
		// TODO replace with mixin?
		MethodNode drawScreenMethod = getMethod(LabyModCoreMod.isObfuscated() ? "a" : "drawScreen", "(IIF)V");

		LabelNode endNode = new LabelNode();

		ListIterator<AbstractInsnNode> renderPlusBegin = getInstructionsAtLine(drawScreenMethod, 145);
		renderPlusBegin.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(GuiChatFilterTransformer.class), "shouldShowAddButton", "()Z", false));
		renderPlusBegin.add(new JumpInsnNode(IFEQ, endNode));

		ListIterator<AbstractInsnNode> renderPlusEnd = getInstructionsAtLine(drawScreenMethod, 149);
		renderPlusEnd.add(endNode);

		MethodNode mouseClickedMethod = getMethod(LabyModCoreMod.isObfuscated() ? "a" : "mouseClicked", "(III)V");

		endNode = new LabelNode();
		ListIterator<AbstractInsnNode> addFilterBegin = getInstructionsAtLine(mouseClickedMethod, 272);
		addFilterBegin.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(GuiChatFilterTransformer.class), "shouldShowAddButton", "()Z", false));
		addFilterBegin.add(new JumpInsnNode(IFEQ, endNode));

		ListIterator<AbstractInsnNode> addFilterEnd = gotoLine(addFilterBegin, 275);
		addFilterEnd.add(endNode);
	}

	public static boolean shouldShowAddButton() {
		return FileProvider.getSingleton(ChatFilterTemplates.class).shouldShowAddButton();
	}

}
