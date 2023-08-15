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

package dev.l3g7.griefer_utils.misc.mysterymod_connection;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.network.MysteryModConnectionEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.PacketDecoder;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;

import static dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection.State.*;

public class MysteryModConnection {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final SocketAddress SERVER_HOST = new InetSocketAddress("server2.mysterymod.net", 5654);
	public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("GrieferUtils' MysteryMod-Connection", false, Thread.MIN_PRIORITY));

	private static State currentState = NOT_CONNECTED;
	private static NioSocketChannel channel;

	public static State getState() {
		return currentState;
	}

	public static void setState(ChannelHandlerContext ctx, State state) {
		currentState = state;
		new MysteryModConnectionEvent.MMStateChangeEvent(ctx, state).fire();
	}

	@OnEnable
	private static void connect() {
		setState(null, CONNECTING);

		LOGGER.info("Connecting to {}", SERVER_HOST);
		Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<NioSocketChannel>() {
				protected void initChannel(NioSocketChannel ch) {
					channel = ch;
					ch.pipeline()
						.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4))
						.addLast(new PacketDecoder())
						.addLast(new LengthFieldPrepender(4))
						.addLast(new PacketEncoder())
						.addLast(new PacketHandler());
				}
			});
		try {
			bootstrap.connect(SERVER_HOST).get();
		} catch (NullPointerException e) {
			// Address could not be resolved, probably no internet
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@EventListener
	public static void onServerJoin(ServerEvent.ServerJoinEvent event) {
		if (getState() != CONNECTED)
			connect();
	}

	@EventListener
	public static void onAccountSwitch(AccountSwitchEvent event) {
		if (getState() == CONNECTED)
			channel.close();

		new Thread(MysteryModConnection::connect).start();
	}

	public enum State {

		CONNECTED("Verbunden"),
		INVALID_SESSION("Die Session ist ungültig!"),
		SESSION_SERVERS_UNAVAILABLE("Die Session-Server sind nicht erreichbar!"),
		ERROR_OCCURRED("Es gab einen Fehler!"),
		CONNECTING("Verbindet..."),
		NOT_CONNECTED("Nicht verbunden");

		public final String errorMessage;
		State(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

}
