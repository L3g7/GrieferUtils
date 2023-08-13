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

package dev.l3g7.griefer_utils.event.events.render;

import de.emotechat.addon.gui.chat.render.EmoteChatRenderer;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public abstract class ChatLineEvent extends Event {

	public static class ChatLineInitEvent extends ChatLineEvent {

		public final IChatComponent component;
		public final boolean secondChat;

		public ChatLineInitEvent(IChatComponent component, boolean secondChat) {
			this.component = component;
			this.secondChat = secondChat;
		}

		@Mixin(value = GuiChatAdapter.class, remap = false)
		private static class MixinGuiChatAdapter {

			@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
			public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
				MinecraftForge.EVENT_BUS.post(new ChatLineEvent.ChatLineInitEvent(component, secondChat));
			}

		}

	}

	public static class ChatLineAddEvent extends ChatLineEvent {

		public final ChatLine chatLine;

		public ChatLineAddEvent(ChatLine chatLine) {
			this.chatLine = chatLine;
		}

		@Mixin(value = EmoteChatRenderer.class, remap = false)
		private static class MixinEmoteChatRenderer {

			boolean refreshing;

			@Redirect(method = "addChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"), remap = false)
			public void postChatLineAddEvent(List<Object> instance, int i, Object e) {
				if (!refreshing)
					MinecraftForge.EVENT_BUS.post(new ChatLineEvent.ChatLineAddEvent((ChatLine) e));
				instance.add(i, e);
			}

			@Inject(method = "addChatLine", at = @At("HEAD"))
			public void injectAddChatLine(String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor, boolean refresh, CallbackInfoReturnable<Boolean> cir) {
				refreshing = refresh;
			}
		}

		@Mixin(value = ChatRenderer.class, remap = false)
		private static class MixinChatRenderer {

			private boolean refreshing = false;

			@Redirect(method = "addChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
			public void postChatLineAddEvent(List<Object> instance, int i, Object e) {
				if (!refreshing)
					MinecraftForge.EVENT_BUS.post(new ChatLineEvent.ChatLineAddEvent((ChatLine) e));
				instance.add(i, e);
			}

			@Inject(method = "addChatLine", at = @At("HEAD"))
			public void injectAddChatLine(String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor, boolean refresh, CallbackInfo ci) {
				refreshing = refresh;
			}

		}

	}

}