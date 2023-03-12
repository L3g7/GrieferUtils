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

import dev.l3g7.griefer_utils.event.events.render.RenderChatEvent;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.utils.DrawUtils;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ChatRenderer.class)
public class MixinChatRenderer {

	@Inject(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/DrawUtils;drawStringWithShadow(Ljava/lang/String;DDI)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false)
	private void injectRenderChat(int updateCounter, CallbackInfo ci, DrawUtils draw, int fontHeight, float scale, int chatLineCount, boolean chatOpen, float opacity, int width, int visibleMessages, double totalMessages, double animationSpeed, float lineHeight, double shift, double posX, double posY, int i, Iterator<ChatLine> chatLineIterator, ChatLine chatline, boolean firstLine, boolean lastLine, int updateCounterDifference, int alpha, int x, int y) {
		MinecraftForge.EVENT_BUS.post(new RenderChatEvent(chatline, y, alpha / 255f));
	}

}
