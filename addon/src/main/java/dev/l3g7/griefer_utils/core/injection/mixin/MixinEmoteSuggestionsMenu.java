/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.injection.mixin;

import de.emotechat.addon.gui.chat.suggestion.EmoteSuggestionsMenu;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.chat.SplitLongMessages;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmoteSuggestionsMenu.class, remap = false)
public class MixinEmoteSuggestionsMenu {

	@Shadow
	private GuiTextField textField;
	@Shadow
	private int minecraftTextFieldLength;

	private int textFieldLength;

	@Inject(method = "adjustTextFieldLength", at = @At("HEAD"), remap = false)
	public void injectAdjustTextFieldLength(CallbackInfo ci) {
		textFieldLength = minecraftTextFieldLength;
		String text = textField.getText();

		if (FileProvider.getSingleton(SplitLongMessages.class).isEnabled() && text.startsWith("/msg ") || text.startsWith("/r ") || !text.startsWith("/"))
			minecraftTextFieldLength = Integer.MAX_VALUE;
		else
			minecraftTextFieldLength = textFieldLength;
	}

	@Inject(method = "adjustTextFieldLength", at = @At("TAIL"), remap = false)
	public void injectAdjustTextFieldLengthTail(CallbackInfo ci) {
		minecraftTextFieldLength = textFieldLength;
	}

}
