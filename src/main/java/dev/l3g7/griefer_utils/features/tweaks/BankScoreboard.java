package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MMCustomPayloadEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Optional;

@Singleton
public class BankScoreboard extends Feature {

    private static long bankBalance = -1;

    public static long getBankBalance() {
        return bankBalance;
    }

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Bankguthaben im Scoreboard")
            .config("tweaks.bank_scoreboard.active")
            .icon("bank")
            .defaultValue(true);

    public BankScoreboard() {
        super(Category.TWEAK);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onMMCustomPayload(MMCustomPayloadEvent event) {
        if (event.getChannel().equals("bank"))
            bankBalance = event.getPayload().getAsJsonObject().get("amount").getAsLong();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!ServerCheck.isOnGrieferGames() || world() == null)
            return;

        Scoreboard sb = world().getScoreboard();
        ScoreObjective so = sb.getObjectiveInDisplaySlot(1);

        // Check if scoreboard is loading
        Optional<Score> playTimeScore = sb.getSortedScores(so).stream().filter(s -> s.getScorePoints() == 3).findFirst();
        if (!playTimeScore.isPresent())
            return;
        Optional<ScorePlayerTeam> playTimeTeam = sb.getTeams().stream().filter(t -> t.getMembershipCollection().contains(playTimeScore.get().getPlayerName())).findFirst();
        if (!playTimeTeam.isPresent() || (playTimeTeam.get().getColorPrefix().equals("§f§oLaden")))
            return;

        if (isActive()) {
            // Inject scores if needed
            if (sb.getSortedScores(so).stream().noneMatch(e -> e.getScorePoints() == 17)) {
                moveScores(7, 17, 3); // Make space for injected scores

                // Inject scores
                // Using three color codes to avoid collision with existing objectives
                sb.getValueFromObjective("§0§1§7ᐅ §3§lBankguthaben", so).setScorePoints(10); // Title
                sb.getValueFromObjective("§0§2§f",                   so).setScorePoints(9); // Value
                sb.getValueFromObjective("§0§3§f",                   so).setScorePoints(8); // Space

                // Initialize team
                if (sb.getTeam("GrieferUtilsBank") == null)
                    sb.createTeam("GrieferUtilsBank");
                sb.addPlayerToTeam("§0§2§f", "GrieferUtilsBank");
            }

            // Update suffix of team
            sb.getTeam("GrieferUtilsBank").setNameSuffix(bankBalance == -1 ? "?" : Constants.DECIMAL_FORMAT_98.format(bankBalance) + "$");

        } else {
            // Revert changes
            if (sb.getSortedScores(so).stream().anyMatch(e -> e.getScorePoints() == 17)) {
                moveScores(7, 10, -99); // Move injected scores out of visible area
                moveScores(10, 17, -3); // Move old scores back to correct position
            }
        }
    }

    private void moveScores(int minVal, int maxVal, int delta) {
        Scoreboard sb = world().getScoreboard();
        sb.getSortedScores(sb.getObjectiveInDisplaySlot(1)).stream()
                .filter(e -> e.getScorePoints() > minVal && e.getScorePoints() <= maxVal)
                .forEach(e -> e.setScorePoints(e.getScorePoints() + delta));
    }
}
