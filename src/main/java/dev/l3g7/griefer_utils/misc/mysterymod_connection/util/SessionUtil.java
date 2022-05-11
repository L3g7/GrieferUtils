package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
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
                if (!System.getenv().containsKey("GRIEFER_UTILS_TEST_USERNAME"))
                    throw new RuntimeException("No login information found!");
                try {
                    YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
                    auth.setUsername(System.getenv("GRIEFER_UTILS_TEST_USERNAME"));
                    auth.setPassword(System.getenv("GRIEFER_UTILS_TEST_PASSWORD"));
                    auth.logIn();
                    session = new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString().replace("-", ""), auth.getAuthenticatedToken(), "mojang");
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new RuntimeException("Login failed!");
                }
            }
        }
        return session;
    }

}