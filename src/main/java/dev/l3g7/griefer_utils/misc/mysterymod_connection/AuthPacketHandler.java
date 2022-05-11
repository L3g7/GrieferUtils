package dev.l3g7.griefer_utils.misc.mysterymod_connection;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.PacketReceiveEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.StateChangeEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.*;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.keepalive.KeepAliveACKPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.keepalive.KeepAlivePacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.CryptUtil;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.util.SessionUtil;
import net.minecraft.util.Session;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

import static dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection.*;

@Singleton
public class AuthPacketHandler {

    private static final String MINECRAFT_VERSION = "1.8.9";
    private static final String MYSTERYMOD_VERSION = "1.0.7";

    @EventListener
    public void onKeepAlivePacket(PacketReceiveEvent event) {
        if(event.getPacket() instanceof KeepAlivePacket) {
            KeepAliveACKPacket response = new KeepAliveACKPacket();
            response.setUUID(event.getPacket().getUUID());
            sendPacket(response);
        }
    }

    @EventListener
    public void onRequestAuthPacket(PacketReceiveEvent event) {
        if(event.getPacket() instanceof RequestAuthPacket) {
            sendPacket(new StartAuthPacket(SessionUtil.getSession().getProfile().getId()));
        }
    }

    @EventListener
    public void onCheckAuthPacket(PacketReceiveEvent event) {
        if(event.getPacket() instanceof CheckAuthPacket) {
            state = ((CheckAuthPacket) event.getPacket()).getState();
            EventBus.post(new StateChangeEvent(state));
            if (state == CheckAuthPacket.State.SUCCESSFUL) {
                LOGGER.info("Connected to MysteryMod!");
            } else {
                LOGGER.error("MysteryMod server not reachable: {}", state.name());
            }

        }
    }

    @EventListener
    public void onAuthKeysPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof AuthKeysPacket) {
            Session session = SessionUtil.getSession();
            AuthKeysPacket res = (AuthKeysPacket) event.getPacket();
            PublicKey publicKey = CryptUtil.decodePublicKey(res.getSharedSecret());
            SecretKey secretkey = CryptUtil.generateKey();
            String serverId = new BigInteger(CryptUtil.getServerIdHash(res.getServerId(), CryptUtil.decodePublicKey(res.getSharedSecret()), secretkey)).toString(16);
            try {
                SessionUtil.getSessionService().joinServer(session.getProfile(), session.getToken(), serverId);
                sendPacket(new AuthPacket(session.getProfile().getId(), session.getUsername(), CryptUtil.encryptData(publicKey, secretkey.getEncoded()), CryptUtil.encryptData(publicKey, res.getVerifyToken()), MYSTERYMOD_VERSION, MINECRAFT_VERSION));
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }
    }

}
