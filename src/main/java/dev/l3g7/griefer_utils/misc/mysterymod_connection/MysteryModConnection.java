package dev.l3g7.griefer_utils.misc.mysterymod_connection;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.PacketReceiveEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.PacketDecoder;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.PacketEncoder;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.CheckAuthPacket.State;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class MysteryModConnection {

    private static final String SERVER_HOST = "server2.mysterymod.net";
    private static final int SERVER_PORT = 5654;

    private static NioSocketChannel channel;

    static State state = State.LOADING;
    static final Logger LOGGER = LogManager.getLogger();

    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("GrieferUtils' MysteryMod-Connection", false, Thread.MIN_PRIORITY));

    public static EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public static void sendPacket(Packet packet) {
        eventLoopGroup.submit(() -> channel.writeAndFlush(packet));
    }

    public static void connect() {
        if (state == State.SUCCESSFUL) {
            try {
                channel.close().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("Connecting to {}:{}", SERVER_HOST, SERVER_PORT);
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
                                .addLast(new SimpleChannelInboundHandler<Packet>() {
                                    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
                                        EventBus.post(new PacketReceiveEvent(packet));
                                    }
                                });
                    }
                });
        try {
            bootstrap.connect(SERVER_HOST, SERVER_PORT).get();
        } catch (NullPointerException e) {
            // Address could not be resolved, probably no internet
        } catch (InterruptedException | ExecutionException e) {
	        state = State.SESSION_SERVERS_DOWN;
            e.printStackTrace();
        }
    }

}