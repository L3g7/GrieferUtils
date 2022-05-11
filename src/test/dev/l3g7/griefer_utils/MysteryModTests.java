package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.AuthPacketHandler;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.PacketReceiveEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.Packet;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.CheckAuthPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.Transaction;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.SessionUtil;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MysteryModTests {

    private Consumer<Packet> packetConsumer;

    public MysteryModTests() {
        SessionUtil.getSession();
        EventBus.register(this);
        EventBus.register(new AuthPacketHandler());
        MysteryModConnection.connect();
    }

    @Test
    @Timeout(10)
    public void testTransactions() {
        CompletableFuture<List<Transaction>> future = new CompletableFuture<>();
        packetConsumer = packet -> {
            if (packet instanceof CheckAuthPacket)
                MysteryModConnection.sendPacket(new RequestTransactionsPacket(SessionUtil.getSession().getProfile().getId()));
            if (packet instanceof TransactionsPacket)
                future.complete(((TransactionsPacket) packet).getTransactions());
        };
        assertDoesNotThrow(() -> future.get());
    }

    @EventListener
    public void onPacketReceive(PacketReceiveEvent event) {
        if (packetConsumer != null)
            packetConsumer.accept(event.getPacket());
    }
}
