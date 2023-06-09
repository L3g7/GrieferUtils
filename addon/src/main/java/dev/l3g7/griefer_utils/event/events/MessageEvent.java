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

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.mixin.labymod.MixinGuiChatAdapter;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.main.LabyMod;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

/**
 * A forge event for message processing.
 */
public class MessageEvent extends Event {

	/**
	 * A forge event for LabyMod's {@link MessageModifyChatEvent}.
	 */
	public static class MessageModifyEvent extends MessageEvent {

		public final IChatComponent original;
		public IChatComponent message;

		public MessageModifyEvent(IChatComponent original) {
			this.original = original;
			message = original.createCopy();
		}

		@OnEnable
		private static void register() {
			LabyMod.getInstance().getEventManager().register((MessageModifyChatEvent) o -> {
				MessageModifyEvent event = new MessageModifyEvent((IChatComponent) o);
				EVENT_BUS.post(event);
				return event.message;
			});
		}

	}

	/**
	 * Posted in {@link MixinGuiChatAdapter#postMessageModifiedEvent(IChatComponent, int, int, boolean, boolean, String, Integer, CallbackInfo)}
	 */
	public static class MessageModifiedEvent extends MessageEvent {

		public final IChatComponent component;

		public MessageModifiedEvent(IChatComponent component) {
			this.component = component;
		}

	}

	/**
	 * A forge event for LabyMod's {@link net.labymod.api.events.MessageSendEvent}.
	 */
	@Cancelable
	public static class MessageSendEvent extends MessageEvent {

		public static boolean post(String message) {
			if (!EVENT_BUS.post(new MessageSendEvent(message))) {
				for (net.labymod.api.events.MessageSendEvent lmEvent : labyMod().getEventManager().getMessageSend())
					if (lmEvent.onSend(message))
						return true;

				return false;
			}

			return true;
		}

		public final String message;

		private MessageSendEvent(String message) {
			this.message = message;
		}

		@OnEnable
		private static void register() {
			LabyMod.getInstance().getEventManager().register((net.labymod.api.events.MessageSendEvent) s -> EVENT_BUS.post(new MessageSendEvent(s)));
		}

	}

}
