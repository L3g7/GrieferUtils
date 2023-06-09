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

package dev.l3g7.griefer_utils.mixin;

import de.emotechat.addon.gui.chat.render.EmoteChatRenderer;
import dev.l3g7.griefer_utils.event.events.render.ChatLineEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderChatEvent;
import net.labymod.ingamechat.renderer.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmoteChatRenderer.class, remap = false)
public class MixinEmoteChatRenderer {

	@Inject(method = "drawLine", at = @At("TAIL"))
	public void injectDrawLine(FontRenderer font, ChatLine chatLine, float x, float y, int width, int alpha, int mouseX, int mouseY, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new RenderChatEvent(chatLine, (int) y + 8, alpha / 255f));
	}

	@Redirect(method = "addChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"), remap = false)
	public void postChatLineAddEvent(List<Object> instance, int i, Object e) {
		MinecraftForge.EVENT_BUS.post(new ChatLineEvent.ChatLineAddEvent((ChatLine) e));
		instance.add(i, e);
	}


}
