package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.util.Session;

import java.net.Proxy;
import java.util.UUID;

import static dev.l3g7.griefer_utils.features.Feature.mc;

public class SessionUtil {

    private static MinecraftSessionService sessionService = null;
    private static Session session = null;

    public static MinecraftSessionService getSessionService() {
        if (sessionService == null) {
            if (mc() != null)
                sessionService = mc().getSessionService();
            else
                sessionService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()).createMinecraftSessionService();
        }

        return sessionService;
    }

    public static Session getSession() {
        if (session == null) {
            if (mc() != null) {
                session = mc().getSession();
            } else {
                if (!System.getenv().containsKey("MINECRAFT_TEST_USERNAME"))
                    throw new RuntimeException("No login information found!");
                try {
                    session = MicrosoftAuth.loginWithCredentials(System.getenv("MINECRAFT_TEST_USERNAME"), System.getenv("MINECRAFT_TEST_PASSWORD"));
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new RuntimeException("Login failed!");
                }
            }
        }
        return session;
    }

}