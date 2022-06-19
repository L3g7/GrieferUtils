package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.net.Proxy;
import java.util.UUID;

public class SessionUtil {

    private static MinecraftSessionService sessionService = null;
    private static Session session = null;

    public static MinecraftSessionService getSessionService() {
        if (sessionService == null) {
            if (Minecraft.getMinecraft() != null)
                sessionService = Minecraft.getMinecraft().getSessionService();
            else
                sessionService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()).createMinecraftSessionService();
        }

        return sessionService;
    }

    public static Session getSession() {
        if (session == null) {
            if (Minecraft.getMinecraft() != null) {
                session = Minecraft.getMinecraft().getSession();
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