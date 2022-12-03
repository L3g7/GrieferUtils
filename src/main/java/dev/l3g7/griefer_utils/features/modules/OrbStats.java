package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ChatQueue;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.Feature.isOnGrieferGames;
import static dev.l3g7.griefer_utils.misc.Constants.DECIMAL_FORMAT_3;

@Singleton
public class OrbStats extends Module {

	private static final Pattern RANKING_PATTERN = Pattern.compile("§7(?<item>.*): §e(?<amount>[0-9]+).*");

	private HashMap<Integer, Integer> stats = new HashMap<>();
	private String lastItem = null;
	private boolean waitingForGUI = false;
	private GuiScreen lastScreen = null;
	private CompletableFuture<Void> guiInitBlock = null;

	public OrbStats() {
		super("OrbStatistik", "Zeigt dir an, wie oft das zuletzt abgegebene Item insgesamt abgegeben wurde.", "orb_stats", new ControlElement.IconData(Material.EXP_BOTTLE));
	}

	public void loadSettings() {
		getBooleanElement().addCallback(v -> {
		// If no data is found, open and close /stats automatically
		if (v && stats.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForGUI) {
			guiInitBlock = ChatQueue.sendBlocking("/stats", "/stats geht nicht!");
			waitingForGUI = true;
			lastScreen = mc.currentScreen;
		}
	});
	}

	@Override
	public String[] getValues() {
		return lastItem == null ? getDefaultValues() : new String[] {lastItem + ": " + DECIMAL_FORMAT_3.format(stats.get(lastItem.hashCode()))};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"?"};
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isActive() || !ServerCheck.isOnCitybuild() || !(mc.currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc.currentScreen, "lowerChestInventory", "field_147015_w", "w");

		// When the players name contains a word that was blacklisted at some point, it is not included in the title
		// (Source: 75c4a4bd-2dcf-46a2-b8f1-e5f44ce120db / bc1f3d61-0878-4006-ba46-fb479fc37a1e)
		if (!inv.getName().equals("§6Statistik von §e" + PlayerUtil.getName()) && !inv.getName().equals("§6Statistik"))
			return;

		// Check if it's the users stats that are open
		ItemStack skull = inv.getStackInSlot(10);
		String uuid = ItemUtil.getUUIDFromSkullTexture(skull);

		if (uuid == null || !uuid.equalsIgnoreCase(PlayerUtil.getUUID().toString()))
			return;

		// Inv hasn't been loaded yet
		if (inv.getStackInSlot(42) == null || inv.getStackInSlot(42).getItem() != Items.wheat)
			return;

		boolean itemWasNull = lastItem == null;

		for (int i = 0; i < inv.getSizeInventory(); i++)
			extractInfo(inv.getStackInSlot(i), itemWasNull);

		if (waitingForGUI) {
			guiInitBlock.complete(null);
			mc.displayGuiScreen(lastScreen);
			lastScreen = null;
			waitingForGUI = false;
		}

		saveConfig();
	}

	@EventListener
	public void onCBJoin(CityBuildJoinEvent event) {
		System.out.println("AAAAAAAA" + isActive() + isOnGrieferGames());
		if (!isActive() || !isOnGrieferGames())
			return;

		System.out.println("BBBBBBBBBBBBB");

		// If no data is found, open and close /stats automatically
		if (stats.isEmpty()) {
			System.out.println("TRIGGERING");
			guiInitBlock = ChatQueue.sendBlocking("/stats", "/stats geht nicht!");
			waitingForGUI = true;
		} else System.out.println(stats);
	}

	@EventListener
	public void onMsgReceive(MessageReceiveEvent event) {
		Matcher matcher = Constants.ORB_SELL_PATTERN.matcher(event.getUnformatted());
		if (!matcher.matches()) {
			System.out.println(event.getUnformatted());
			System.out.println(matcher.pattern().pattern());
			return;
		}

		lastItem = matcher.group("item");

		// Add the received orbs
		int addend = Integer.parseInt(matcher.group("amount").replace(".", ""));
		stats.compute(lastItem.hashCode(), (key, value) -> (value == null ? 0 : value) + addend);
	}

	private void extractInfo(ItemStack stack, boolean itemWasNull) {
		if (stack == null || !stack.hasTagCompound())
			return;

		NBTTagCompound tag = stack.getTagCompound();
		NBTTagList lore = tag.getCompoundTag("display").getTagList("Lore", 8);

		for (int i = 0; i < lore.tagCount(); i++) {
			String line = lore.getStringTagAt(i);
			if (!line.contains("§7- §e§8> "))
				continue;

			Matcher matcher = RANKING_PATTERN.matcher(line);
			if (!matcher.find())
				continue;


			int amount = Integer.parseInt(matcher.group("amount"));
			if (amount == 0)
				continue;

			String item = matcher.group("item");

			// If no item was last used, it is set to the one with the highest amount
			if (itemWasNull && (lastItem == null || stats.get(lastItem.hashCode()) < amount))
				lastItem = item;

			stats.put(item.hashCode(), amount);
		}
	}

	@EventListener
	public void loadConfig(ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		lastItem = null;
		stats.clear();

		String path = "modules.orb_stats.stats." + PlayerUtil.getUUID();

		if (Config.has(path + ".last"))
			lastItem = Config.get(path + ".last").getAsString();
		if (Config.has(path + ".data"))
			stats = HashMapSerializer.fromString(Config.get(path + ".data").getAsString());
	}

	private void saveConfig() {
		String path = "modules.orb_stats.stats." + PlayerUtil.getUUID();
		Config.set(path + ".last", lastItem);
		Config.set(path + ".data", HashMapSerializer.toString(stats));
		Config.save();
	}

	private static class HashMapSerializer {
		public static String toString(HashMap<Integer, Integer> map) {
			ByteBuffer buf = ByteBuffer.allocate(map.size() * 8);

			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				buf.putInt(entry.getKey());
				buf.putInt(entry.getValue());
			}

			return Base64.getEncoder().encodeToString(buf.array());
		}

		public static HashMap<Integer, Integer> fromString(String b64) {
			HashMap<Integer, Integer> map = new HashMap<>();
			ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(b64));

			while (buf.hasRemaining())
				map.put(buf.getInt(), buf.getInt());

			return map;
		}

	}

}
