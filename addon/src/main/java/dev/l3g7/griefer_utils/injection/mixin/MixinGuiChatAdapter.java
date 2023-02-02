package dev.l3g7.griefer_utils.injection.mixin;

import dev.l3g7.griefer_utils.util.misc.ChatLogModifier;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiChatAdapter.class)
public class MixinGuiChatAdapter {

	@Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"), remap = false)
	public void log(Logger logger, String message) {
		String moddedMsg = ChatLogModifier.modifyMessage(message);

		if (moddedMsg != null)
			logger.info(moddedMsg);
	}

}
