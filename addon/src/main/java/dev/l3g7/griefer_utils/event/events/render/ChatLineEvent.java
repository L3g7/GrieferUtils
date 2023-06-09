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

import dev.l3g7.griefer_utils.mixin.MixinChatRenderer;
import dev.l3g7.griefer_utils.mixin.MixinGuiChatAdapter;
import net.labymod.ingamechat.renderer.ChatLine;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public abstract class ChatLineEvent extends Event {

	/**
	 * Posted in {@link MixinGuiChatAdapter#postChatLineInitEvent(IChatComponent, int, int, boolean, boolean, String, Integer, CallbackInfo)}
	 */
	public static class ChatLineInitEvent extends ChatLineEvent {

		public final IChatComponent component;

		public ChatLineInitEvent(IChatComponent component) {
			this.component = component;
		}
	}

	/**
	 * Posted in {@link MixinChatRenderer#postChatLineAddEvent(List, int, Object)}
	 */
	public static class ChatLineAddEvent extends ChatLineEvent {

		public final ChatLine chatLine;

		public ChatLineAddEvent(ChatLine chatLine) {
			this.chatLine = chatLine;
		}

	}

}