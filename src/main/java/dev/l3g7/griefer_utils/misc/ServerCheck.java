package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerQuitEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerSwitchEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;

@Singleton
public class ServerCheck {

    private static boolean onGrieferGames;
    private static boolean onCitybuild;

    public static boolean isOnGrieferGames() {
        return onGrieferGames;
    }

    public static boolean isOnCitybuild() {
        return onCitybuild;
    }

    @EventListener(priority = EventPriority.HIGHEST)
    public void onServerJoin(ServerJoinEvent event) {
        String server = event.getData().getIp().toLowerCase();
        if (server.endsWith("griefergames.net") || server.endsWith("griefergames.de")) {
            onGrieferGames = true;
        }
    }

    @EventListener
    public void onCitybuildJoin(CityBuildJoinEvent event) {
        onCitybuild = true;
    }

    @EventListener
    public void onServerSwitch(ServerSwitchEvent event) {
        onCitybuild = false;
    }

    @EventListener(priority = EventPriority.LOWEST)
    public void onServerQuit(ServerQuitEvent event) {
        onGrieferGames = false;
    }

}
