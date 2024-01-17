/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import dev.l3g7.griefer_utils.event.events.network.MysteryModConnectionEvent.MMPacketReceiveEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection.State;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.auth.*;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.keep_alive.KeepAliveACKPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packets.keep_alive.KeepAlivePacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.CryptUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.util.Session;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class PacketHandler extends SimpleChannelInboundHandler<Packet> {

	private static final String MINECRAFT_VERSION = "1.8.9";
	private static final String MYSTERY_MOD_VERSION = "1.0.7";

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		if (packet instanceof KeepAlivePacket)
			ctx.writeAndFlush(new KeepAliveACKPacket());

		else if (packet instanceof LoginRequestPacket)
			ctx.writeAndFlush(new LoginStartPacket(mc().getSession().getProfile().getId()));

		else if (packet instanceof LoginEncryptionPacket) {
			LoginEncryptionPacket res = (LoginEncryptionPacket) packet;
			Session session = mc().getSession();
			PublicKey publicKey = CryptUtil.decodePublicKey(res.sharedSecret);
			SecretKey secretkey = CryptUtil.generateKey();
			String serverId = new BigInteger(CryptUtil.getServerIdHash(res.serverId, CryptUtil.decodePublicKey(res.sharedSecret), secretkey)).toString(16);
			try {
				mc().getSessionService().joinServer(session.getProfile(), session.getToken(), serverId);
				ctx.writeAndFlush(new LoginAuthenticationPacket(session.getProfile().getId(), session.getUsername(), CryptUtil.encryptData(publicKey, secretkey.getEncoded()), CryptUtil.encryptData(publicKey, res.verifyToken), MYSTERY_MOD_VERSION, MINECRAFT_VERSION));
			} catch (AuthenticationUnavailableException e) {
				e.printStackTrace();
				MysteryModConnection.setState(ctx, State.SESSION_SERVERS_UNAVAILABLE);
			} catch (AuthenticationException e) {
				if (LabyModCoreMod.isObfuscated())
					e.printStackTrace();
				MysteryModConnection.setState(ctx, State.INVALID_SESSION);
				System.out.println("Login using " + session.getProfile() + " was invalid!");
			}
		}

		else if (packet instanceof LoginResponsePacket)
			MysteryModConnection.setState(ctx, ((LoginResponsePacket) packet).state);

		new MMPacketReceiveEvent<>(ctx, packet).fire();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
	}

}
