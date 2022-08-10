package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.event.events.SettingsUpdateEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.MysteryModConnection;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.PacketReceiveEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.event.StateChangeEvent;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.auth.CheckAuthPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.RequestTransactionsPacket;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.Transaction;
import dev.l3g7.griefer_utils.misc.mysterymod_connection.packet.transactions.TransactionsPacket;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class Transactions extends Feature {

    private final CategorySetting element = new CategorySetting()
            .name("§eTransaktionen")
            .icon("mysterymod")
            .description("§eVerbindet...")
            .settingsEnabled(false)
            .subSettings(
                    new HeaderSetting("§r"),
                    new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
                    new HeaderSetting("§f§lTransaktionen").scale(.7).entryHeight(7),
                    new HeaderSetting("§fder letzten 30 Tage").scale(.7).entryHeight(10),
                    new HeaderSetting("§c§nDie Beträge sind abgerundet§c!").scale(.7));

    private List<Transaction> transactions = Collections.emptyList();

    public Transactions() {
        super(Category.MISC);
    }

    @Override
    public SettingsElement getMainElement() {
        return element;
    }

    @LateInit
    public void connect() {
        MysteryModConnection.connect();
    }

    @EventListener
    public void onStateChange(StateChangeEvent event) {
        if (event.getState() == CheckAuthPacket.State.SUCCESSFUL) {
            element.name("Transaktionen")
                    .description(null)
                    .settingsEnabled(true);

            // Send Transaction packet every 10s
            UUID uuid = Minecraft.getMinecraft().getSession().getProfile().getId();
            MysteryModConnection.getEventLoopGroup().scheduleAtFixedRate(() -> MysteryModConnection.sendPacket(new RequestTransactionsPacket(uuid)), 0, 10, TimeUnit.SECONDS);
        } else {
            element.name("§c§mTransaktionen")
                    .description("§c§oMysteryMod ist nicht erreichbar! (" + event.getState().name() + ")")
                    .settingsEnabled(false);
        }
    }

    @EventListener
    public void onTransactionResponse(PacketReceiveEvent event) {
        if (event.getPacket() instanceof TransactionsPacket) {
            // Populate transactions
            transactions = ((TransactionsPacket) event.getPacket()).getTransactions();
            Collections.reverse(transactions); // So newest entry is first
        }
    }

    private boolean wasTransactionsOpened = false;

    @EventListener
    public void onSettingsUpdate(SettingsUpdateEvent event) {
        List<SettingsElement> list = event.getList();

        // Check if transactions are open
        if (list.isEmpty() || list.get(0) != element.getSubSettings().getElements().get(0)) {
            wasTransactionsOpened = false;
            return;
        }

        if (!wasTransactionsOpened) {
            // Send RequestTransactionsPacket upon opening transactions, so they are up-to-date
            MysteryModConnection.sendPacket(new RequestTransactionsPacket(Minecraft.getMinecraft().getSession().getProfile().getId()));
            wasTransactionsOpened = true;
        }

        list.clear();
        list.addAll(element.getSubSettings().getElements());

        // Add transactions count
        list.add(new HeaderSetting("Insgesamt " + (transactions.size() == 1 ? "eine Transaktion" : transactions.size() + " Transaktionen")));
        list.add(new HeaderSetting("§r").scale(.4).entryHeight(10));

        // Add transactions
        for (Transaction t : transactions) {
            Direction direction = Direction.get(t);

            String amountStr = Constants.DECIMAL_FORMAT_98.format(Double.parseDouble(t.getAmount()));
            String title = "§l" + amountStr + "$§";

            ArrayList<SettingsElement> subSettings = new ArrayList<>(Arrays.asList(
                    new HeaderSetting("§r"),
                    new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
                    new HeaderSetting("§f§lTransaktion #" + t.getId()).scale(.7)
            ));

            // Add sender/receiver by direction
            switch (direction) {
                case SENT:
                    title = "§c" + title + "c an §l" + t.getReceiverName();
                    subSettings.add(new HeaderSetting("§lEmpfänger: §r" + t.getReceiverName()).entryHeight(11));
                    break;
                case RECEIVED:
                    title = "§a" + title + "a von §l" + t.getSenderName();
                    subSettings.add(new HeaderSetting("§lSender: §r" + t.getSenderName()).entryHeight(11));
                    break;
                case SELF:
                    title = "§e" + title + "e an dich";
                    break;
            }

            // Add amount and timestamp
            subSettings.add(new HeaderSetting("§lBetrag: §r" + amountStr + "$").entryHeight(11));
            subSettings.add(new HeaderSetting("§lZeitpunkt: §r" + Constants.DATE_FORMAT.format(new Date(t.getTimestamp()))).entryHeight(11));

            list.add(new CategorySetting().name(" " + title).subSettings(subSettings));
        }
    }

    private enum Direction {

        SENT, RECEIVED, SELF;

        public static Direction get(Transaction t) {
            if (t.getReceiverName().equals(t.getSenderName()))
                return SELF;
            if (Minecraft.getMinecraft().getSession().getUsername().equals(t.getSenderName()))
                return SENT;
            else
                return RECEIVED;
        }
    }
}
