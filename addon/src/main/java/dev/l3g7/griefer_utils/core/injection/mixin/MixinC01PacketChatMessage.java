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

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.chat.SplitLongMessages;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(C01PacketChatMessage.class)
public class MixinC01PacketChatMessage {

	@ModifyConstant(method = "<init>(Ljava/lang/String;)V", constant = @Constant(intValue = 100))
	private int modify(int constant) {
		return FileProvider.getSingleton(SplitLongMessages.class).isEnabled() ? Integer.MAX_VALUE : constant;
	}

}
