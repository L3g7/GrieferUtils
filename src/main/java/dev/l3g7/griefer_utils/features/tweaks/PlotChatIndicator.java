package dev.l3g7.griefer_utils.features.tweaks;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Array;
import java.util.List;

@Singleton
public class PlotChatIndicator extends Feature {

	private final List<String> specialServers = ImmutableList.of("Nature", "Extreme", "CBE", "Event");
	private StringBuilder states; // A StringBuilder is used since it has .setChatAt, and with HashMaps you'd have 26 entries per account in the config)
	private String server;

    private Boolean plotchatState = null;
    private boolean waitingForPlotchatStatus = false;

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Plot-Chat-Indikator")
            .description("Zeichnet einen orangen Rahmen um die Chateingabe, wenn der Plotchat aktiviert ist.")
            .config("tweaks.plot_chat_indicator.active")
            .icon("speech_bubble")
            .defaultValue(false)
            .callback(v -> {
                if (v && isOnCityBuild() && plotchatState == null && !waitingForPlotchatStatus) {
                    waitingForPlotchatStatus = true;
                    sendQueued("/p chat");
                }
            });

    public PlotChatIndicator() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onServerSwitch(ServerSwitchEvent event) {
        plotchatState = null;
    }

	@EventListener
	public void onServerJoin(ServerJoinEvent event) {
		if (!isOnGrieferGames())
			return;

		String path = "tweaks.plot_chat_indicator.states." + PlayerUtil.getUUID();
		if (Config.has(path))
			states = new StringBuilder(Config.get(path).getAsString());
		else
			states = new StringBuilder(Strings.repeat("?", 26));
	}

    @EventListener
    public void onCityBuildJoin(CityBuildJoinEvent event) {
        ScorePlayerTeam team = world().getScoreboard().getTeam("server_value");
		server = team == null ? "" : ModColor.removeColor(team.getColorPrefix());

		if (server.equals("Lava") || server.equals("Wasser")) {
			plotchatState = false;
			return;
		}

	    char character = states.charAt(getIndex(server));
		plotchatState = character == '?' ? null : character == 'Y';

		if (plotchatState != null || !isActive())
			return;

		waitingForPlotchatStatus = true;
        sendQueued("/p chat");
    }

    @EventListener
    public void onReceive(MessageReceiveEvent event) {

        // Update plot chat state
        if (event.getFormatted().matches("^§r§8\\[§r§6GrieferGames§r§8] §r§.Die Einstellung §r§.chat §r§.wurde (?:de)?aktiviert\\.§r$")) {
            plotchatState = event.getFormatted().contains(" aktiviert");
	        states.setCharAt(getIndex(server), plotchatState ? 'Y' : 'N');
			Config.set("tweaks.plot_chat_indicator.states." + PlayerUtil.getUUID(), states.toString());
			Config.save();

            if (waitingForPlotchatStatus) {
                waitingForPlotchatStatus = false;
                sendQueued("/p chat");
            }
        }
    }

	private int getIndex(String server) {
		if (specialServers.contains(server))
			return specialServers.indexOf(server) + 22;

		return Integer.parseInt(server.substring(2)) - 1;
	}

    @SubscribeEvent
    public void onRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!isActive() || !isOnGrieferGames() || plotchatState == null || !plotchatState)
            return;

	    GuiScreen gcc = event.gui;
	    if (!(gcc instanceof GuiChat))
	        return;

	    int buttonWidth = gcc instanceof GuiChatCustom ? Array.getLength(Reflection.get(gcc, "chatButtons")) * 14 : 0;
	    int color = 0xFFFFA126;

	    // Render frame
	    GuiScreen.drawRect(2, gcc.height - 14, gcc.width - 2 - buttonWidth, gcc.height - 2, 100 << 24);

	    GuiScreen.drawRect(1, gcc.height - 15, gcc.width - 1 - buttonWidth, gcc.height - 14, color);
	    GuiScreen.drawRect(1, gcc.height - 2, gcc.width - 1 - buttonWidth, gcc.height - 1, color);
	    GuiScreen.drawRect(1, gcc.height - 15, 2, gcc.height - 1, color);
	    GuiScreen.drawRect(gcc.width - 2 - buttonWidth, gcc.height - 15, gcc.width - 1 - buttonWidth, gcc.height - 1, color);
    }

}
